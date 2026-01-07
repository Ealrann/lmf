package org.logoce.lmf.cli.rename;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.ReferenceEditPlanner;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.NodeNameResolver;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.cli.util.LmStringLiteral;
import org.logoce.lmf.cli.workspace.WorkspaceDocuments;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.lang.Named;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class RenamePlanner
{
	private final ReferenceEditPlanner referencePlanner = new ReferenceEditPlanner();

	public RenamePlanResult plan(final WorkspaceDocuments workspace,
								 final String reference,
								 final String newName)
	{
		Objects.requireNonNull(workspace, "workspace");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(newName, "newName");

		try
		{
			final var targetDoc = workspace.targetDocument().document();
			final var source = targetDoc.source();
			final var linkRoots = RootReferenceResolver.collectLinkRoots(targetDoc.linkTrees());
			if (linkRoots.isEmpty())
			{
				throw new RenamePlanException("No link trees available for target document");
			}

			final var targetNode = resolveTargetNode(linkRoots, reference);
			final var targetObject = resolveNamedObject(targetNode);
			final var targetId = ObjectId.from(targetObject);
			if (targetId == null)
			{
				throw new RenamePlanException("Cannot compute object id for " + reference);
			}

			final var normalizedNewName = normalizeNewName(newName);
			final var currentName = NodeNameResolver.resolve(targetNode);
			if (Objects.equals(currentName, normalizedNewName))
			{
				return new RenamePlanResult.Success(new RenamePlannedEdit(java.util.Map.of(), false));
			}

			final boolean allowAnchorRewrite = isUniqueNameInModel(linkRoots, targetId, currentName);

			final var editsByFile = new HashMap<Path, List<TextEdits.TextEdit>>();
			final var nameEdit = planNameEdit(targetNode, source, currentName, normalizedNewName);
			if (nameEdit != null)
			{
				editsByFile.computeIfAbsent(workspace.targetDocument().path(), ignored -> new ArrayList<>()).add(nameEdit);
			}

			final ReferenceEditPlanner.ReferenceMapper mapper = (node, raw, refTargetId, quoted) ->
				rewriteReference(raw, quoted, refTargetId, targetId, currentName, normalizedNewName, allowAnchorRewrite);

			for (final var doc : workspace.documents())
			{
				final var roots = RootReferenceResolver.collectLinkRoots(doc.document().linkTrees());
				if (roots.isEmpty())
				{
					continue;
				}

				final var edits = referencePlanner.planEdits(doc.document().source(), roots, null, mapper, 0);
				if (!edits.isEmpty())
				{
					editsByFile.computeIfAbsent(doc.path(), ignored -> new ArrayList<>()).addAll(edits);
				}
			}

			final var planned = copyEdits(editsByFile);
			return new RenamePlanResult.Success(new RenamePlannedEdit(planned, !planned.isEmpty()));
		}
		catch (RenamePlanException | ReferenceEditPlanner.ReferenceEditException e)
		{
			return new RenamePlanResult.Failure(e.getMessage());
		}
	}

	private static String normalizeNewName(final String value)
	{
		final var unquoted = LmStringLiteral.unquote(value == null ? "" : value.strip());
		if (unquoted.isBlank())
		{
			throw new RenamePlanException("New name must not be empty");
		}
		return unquoted;
	}

	private static LinkNodeInternal<?, PNode, ?> resolveTargetNode(final List<LinkNodeInternal<?, PNode, ?>> roots,
																  final String reference)
	{
		final var resolution = new RootReferenceResolver().resolve(roots, reference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return found.node();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			throw new RenamePlanException("Ambiguous reference: " + reference + " (" + ambiguous.candidates().size() + " matches)");
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			throw new RenamePlanException("Cannot resolve reference: " + notFound.message());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			throw new RenamePlanException("Cannot resolve reference: " + failure.message());
		}

		throw new RenamePlanException("Unexpected reference resolution state");
	}

	private static Named resolveNamedObject(final LinkNodeInternal<?, PNode, ?> node)
	{
		if (node == null)
		{
			throw new RenamePlanException("Missing target node");
		}

		final Object built;
		try
		{
			built = node.build();
		}
		catch (RuntimeException e)
		{
			throw new RenamePlanException("Cannot build target object: " + safeMessage(e));
		}

		if (built instanceof Named named)
		{
			return named;
		}
		throw new RenamePlanException("Target object is not Named (cannot rename)");
	}

	private static TextEdits.TextEdit planNameEdit(final LinkNodeInternal<?, PNode, ?> node,
												  final CharSequence source,
												  final String currentName,
												  final String newName)
	{
		final var tokens = node.pNode() != null ? node.pNode().tokens() : List.<PToken>of();
		if (tokens.isEmpty())
		{
			throw new RenamePlanException("Cannot locate name span (missing tokens)");
		}

		final var index = FeatureValueSpanIndex.build(tokens, source);
		final var nameFeature = Named.Features.NAME.name();
		final var nameFeatures = index.featuresByName(nameFeature);
		if (nameFeatures.size() > 1)
		{
			throw new RenamePlanException("Multiple assignments found for feature '" + nameFeature + "'");
		}

		final var literal = LmStringLiteral.fromUserValue(newName);
		if (!nameFeatures.isEmpty())
		{
			final var span = nameFeatures.getFirst().assignmentSpan();
			return new TextEdits.TextEdit(span.offset(), span.length(), nameFeature + "=" + literal);
		}

		final var positional = resolvePositionalNameSpan(node, index, currentName);
		if (positional != null)
		{
			return new TextEdits.TextEdit(positional.span().offset(), positional.span().length(), literal);
		}

		final int insertionOffset = endOffsetOfLastNonWhitespaceToken(tokens);
		return new TextEdits.TextEdit(insertionOffset, 0, " " + nameFeature + "=" + literal);
	}

	private static FeatureValueSpanIndex.ValueSpan resolvePositionalNameSpan(final LinkNodeInternal<?, PNode, ?> node,
																			final FeatureValueSpanIndex index,
																			final String currentName)
	{
		if (currentName == null || currentName.isBlank())
		{
			return null;
		}

		return index.findValueSpan(null, currentName).orElse(null);
	}

	private static int endOffsetOfLastNonWhitespaceToken(final List<PToken> tokens)
	{
		for (int i = tokens.size() - 1; i >= 0; i--)
		{
			final var token = tokens.get(i);
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			return token.offset() + Math.max(0, token.length());
		}
		throw new RenamePlanException("Cannot locate insertion point (all tokens are whitespace)");
	}

	private static String rewriteReference(final String raw,
										  final boolean quoted,
										  final ObjectId referenceTargetId,
										  final ObjectId renamedId,
										  final String oldName,
										  final String newName,
										  final boolean allowAnchorRewrite)
	{
		if (raw == null || referenceTargetId == null || renamedId == null)
		{
			return null;
		}
		if (oldName == null || oldName.isBlank())
		{
			return null;
		}
		if (Objects.equals(oldName, newName))
		{
			return null;
		}

		if (!referenceTargetId.equals(renamedId))
		{
			if (!allowAnchorRewrite)
			{
				return null;
			}
			if (!Objects.equals(referenceTargetId.modelQualifiedName(), renamedId.modelQualifiedName()))
			{
				return null;
			}
		}

		final var updated = rewriteNameTokens(raw, oldName, newName);
		if (updated == null || updated.equals(raw))
		{
			return null;
		}

		return quoted ? updated : LmStringLiteral.fromUserValue(updated);
	}

	private static boolean isUniqueNameInModel(final List<LinkNodeInternal<?, PNode, ?>> roots,
											  final ObjectId renamedId,
											  final String oldName)
	{
		if (roots == null || roots.isEmpty() || renamedId == null || oldName == null || oldName.isBlank())
		{
			return false;
		}

		ObjectId matchId = null;

		for (final var root : roots)
		{
			for (final var node : root.streamTree().toList())
			{
				final Object built;
				try
				{
					built = node.build();
				}
				catch (RuntimeException ignored)
				{
					continue;
				}

				if (!(built instanceof Named named) || !oldName.equals(named.name()))
				{
					continue;
				}

				final var id = ObjectId.from(named);
				if (id == null)
				{
					continue;
				}

				if (matchId != null && !matchId.equals(id))
				{
					return false;
				}
				matchId = id;
			}
		}

		return renamedId.equals(matchId);
	}

	private static String rewriteNameTokens(final String raw, final String oldName, final String newName)
	{
		if (raw == null || raw.isBlank() || oldName == null || oldName.isBlank())
		{
			return null;
		}

		final var replacements = new ArrayList<Replacement>();
		int cursor = 0;
		while (cursor < raw.length())
		{
			final int at = raw.indexOf('@', cursor);
			final int caret = raw.indexOf('^', cursor);
			final int marker = earliestMarker(at, caret);
			if (marker < 0)
			{
				break;
			}

			final int end = raw.indexOf('/', marker + 1);
			final int nameStart = marker + 1;
			final int nameEnd = end < 0 ? raw.length() : end;

			if (nameEnd <= nameStart)
			{
				cursor = marker + 1;
				continue;
			}

			final var name = raw.substring(nameStart, nameEnd);
			if (Objects.equals(name, oldName))
			{
				replacements.add(new Replacement(nameStart, nameEnd, newName));
			}

			cursor = nameEnd;
		}

		if (replacements.isEmpty())
		{
			return null;
		}

		replacements.sort((a, b) -> Integer.compare(b.start(), a.start()));

		final var builder = new StringBuilder(raw);
		for (final var replacement : replacements)
		{
			if (replacement.start() < 0
				|| replacement.end() > builder.length()
				|| replacement.start() > replacement.end())
			{
				return null;
			}
			builder.replace(replacement.start(), replacement.end(), replacement.text());
		}

		return builder.toString();
	}

	private static int earliestMarker(final int at, final int caret)
	{
		if (at < 0)
		{
			return caret;
		}
		if (caret < 0)
		{
			return at;
		}
		return Math.min(at, caret);
	}

	private static java.util.Map<Path, List<TextEdits.TextEdit>> copyEdits(final HashMap<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		final var copy = new HashMap<Path, List<TextEdits.TextEdit>>();
		for (final var entry : editsByFile.entrySet())
		{
			final var path = entry.getKey();
			final var edits = entry.getValue();
			if (path == null || edits == null || edits.isEmpty())
			{
				continue;
			}
			copy.put(path, List.copyOf(edits));
		}
		return java.util.Map.copyOf(copy);
	}

	private static String safeMessage(final RuntimeException exception)
	{
		final var message = exception.getMessage();
		return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
	}

	private record Replacement(int start, int end, String text)
	{
	}

	private static final class RenamePlanException extends RuntimeException
	{
		private RenamePlanException(final String message)
		{
			super(message);
		}
	}
}
