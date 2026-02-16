package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.cli.workspace.WorkspaceDocuments;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public final class InsertPlanner
{
	private static final String INDENT = "\t";
	private final InsertReferenceShiftPlanner shiftPlanner = new InsertReferenceShiftPlanner();

	public InsertPlanResult plan(final WorkspaceDocuments workspace,
								 final String targetReference,
								 final String formattedSubtree)
	{
		Objects.requireNonNull(workspace, "workspace");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(formattedSubtree, "formattedSubtree");

		try
		{
			final var targetDocument = workspace.targetDocument();
			final var document = targetDocument.document();
			final var source = document.source();

			final var slot = InsertSlotReference.parse(targetReference);
			final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
			if (linkRoots.isEmpty())
			{
				throw new InsertPlanException("No link trees available for target document");
			}

			final var parentNode = resolveNode(linkRoots, slot.parentReference(), "parent reference");
			final var relation = resolveContainmentRelation(parentNode, slot.featureName());
			if (relation == null)
			{
				throw new InsertPlanException("Unknown containment relation '" + slot.featureName() + "' on " + slot.parentReference());
			}
			if (!relation.contains())
			{
				throw new InsertPlanException("Relation '" + slot.featureName() + "' is not a containment");
			}

			final var editsByFile = new HashMap<Path, List<TextEdits.TextEdit>>();
			final var rewrites = new ArrayList<ReferenceRewrite>();

			if (relation.many())
			{
				if (slot.index().isEmpty())
				{
					throw new InsertPlanException("Containment list '" + slot.featureName() + "' requires an index (for example '" + targetReference + ".0')");
				}
				final int index = slot.index().getAsInt();
				final var insertion = planListInsert(source,
													 targetDocument.path(),
													 parentNode,
													 slot.featureName(),
													 index,
													 formattedSubtree,
													 editsByFile);
				if (insertion.shiftContext() != null)
				{
					for (final var doc : workspace.documents())
					{
						shiftPlanner.planDocumentEdits(doc, insertion.shiftContext(), editsByFile, rewrites);
					}
				}
			}
			else
			{
				if (slot.index().isPresent())
				{
					throw new InsertPlanException("Containment '" + slot.featureName() + "' is not a list; remove the index and use '" + slot.parentReference() + "/" + slot.featureName() + "'");
				}
				planSingleInsert(source,
								 targetDocument.path(),
								 parentNode,
								 slot.featureName(),
								 formattedSubtree,
								 editsByFile);
			}

			return new InsertPlanResult.Success(new InsertPlannedEdit(copyEdits(editsByFile), List.copyOf(rewrites)));
		}
		catch (InsertPlanException e)
		{
			return new InsertPlanResult.Failure(e.getMessage());
		}
	}

	private static List<? extends LinkNodeInternal<?, PNode, ?>> listChildren(final LinkNodeInternal<?, PNode, ?> parentNode,
																			 final String featureName)
	{
		return parentNode.streamChildren()
						 .filter(child -> child.containingRelation() != null)
						 .filter(child -> featureName.equals(child.containingRelation().name()))
						 .toList();
	}

	private static PlanResult planListInsert(final CharSequence source,
											final Path targetPath,
											final LinkNodeInternal<?, PNode, ?> parentNode,
											final String featureName,
											final int insertionIndex,
											final String formattedSubtree,
											final Map<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		if (insertionIndex < 0)
		{
			throw new InsertPlanException("Insertion index must be >= 0");
		}

		final var siblings = listChildren(parentNode, featureName);
		if (insertionIndex > siblings.size())
		{
			throw new InsertPlanException("Insertion index out of range: " + insertionIndex + " (size=" + siblings.size() + ")");
		}

		final var insertionEdit = insertionIndex < siblings.size()
								  ? buildInsertBeforeSibling(source, siblings.get(insertionIndex), formattedSubtree)
								  : buildAppendChild(source, parentNode, formattedSubtree);

		editsByFile.computeIfAbsent(targetPath, ignored -> new ArrayList<>()).add(insertionEdit);

		if (insertionIndex == siblings.size())
		{
			return new PlanResult(null);
		}

		final var parentId = resolveObjectId(parentNode, "parent object");
		final var containerPath = parentId.path();
		final var shiftContext = new InsertShiftContext(parentId.modelQualifiedName(),
														containerPath,
														featureName,
														insertionIndex);
		return new PlanResult(shiftContext);
	}

	private static void planSingleInsert(final CharSequence source,
										 final Path targetPath,
										 final LinkNodeInternal<?, PNode, ?> parentNode,
										 final String featureName,
										 final String formattedSubtree,
										 final Map<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		final var existing = listChildren(parentNode, featureName);
		if (!existing.isEmpty())
		{
			throw new InsertPlanException("Containment '" + featureName + "' already has a value; use replace instead");
		}

		final var insertionEdit = buildAppendChild(source, parentNode, formattedSubtree);
		editsByFile.computeIfAbsent(targetPath, ignored -> new ArrayList<>()).add(insertionEdit);
	}

	private static TextEdits.TextEdit buildInsertBeforeSibling(final CharSequence source,
															   final LinkNodeInternal<?, PNode, ?> sibling,
															   final String formattedSubtree)
	{
		final var siblingSpan = locateSpan(source, sibling, "sibling subtree");
		final var baseIndent = trailingIndentBefore(source, siblingSpan.startOffset());
		final var indentedSubtree = indentSubtree(formattedSubtree, baseIndent);
		final var insertionText = indentedSubtree + "\n" + baseIndent;
		return new TextEdits.TextEdit(siblingSpan.startOffset(), 0, insertionText);
	}

	private static TextEdits.TextEdit buildAppendChild(final CharSequence source,
													   final LinkNodeInternal<?, PNode, ?> parentNode,
													   final String formattedSubtree)
	{
		final var parentSpan = locateSpan(source, parentNode, "parent subtree");
		final var baseIndentParent = trailingIndentBefore(source, parentSpan.startOffset());
		final var childIndent = baseIndentParent + INDENT;

		final int closingParenOffset = parentSpan.startOffset() + parentSpan.length() - 1;
		if (closingParenOffset < 0 || closingParenOffset >= source.length() || source.charAt(closingParenOffset) != ')')
		{
			throw new InsertPlanException("Cannot locate parent closing token");
		}

		final int newlineBeforeClosing = lastNewline(source, parentSpan.startOffset(), closingParenOffset);
		final var indentedSubtree = indentSubtree(formattedSubtree, childIndent);

		if (newlineBeforeClosing < 0)
		{
			final var insertionText = "\n" + childIndent + indentedSubtree + "\n" + baseIndentParent;
			return new TextEdits.TextEdit(closingParenOffset, 0, insertionText);
		}

		if (isOnlyWhitespace(source, newlineBeforeClosing + 1, closingParenOffset))
		{
			final int insertionOffset = newlineBeforeClosing + 1;
			final var insertionText = childIndent + indentedSubtree + "\n";
			return new TextEdits.TextEdit(insertionOffset, 0, insertionText);
		}

		final var insertionText = "\n" + childIndent + indentedSubtree;
		return new TextEdits.TextEdit(closingParenOffset, 0, insertionText);
	}

	private static boolean isOnlyWhitespace(final CharSequence source, final int startInclusive, final int endExclusive)
	{
		if (startInclusive < 0 || endExclusive > source.length() || startInclusive > endExclusive)
		{
			return false;
		}

		for (int i = startInclusive; i < endExclusive; i++)
		{
			final char c = source.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r')
			{
				return false;
			}
		}
		return true;
	}

	private static int lastNewline(final CharSequence source, final int start, final int endExclusive)
	{
		for (int i = Math.min(endExclusive - 1, source.length() - 1); i >= start && i >= 0; i--)
		{
			if (source.charAt(i) == '\n')
			{
				return i;
			}
		}
		return -1;
	}

	private static SubtreeSpanLocator.Span locateSpan(final CharSequence source,
													  final LinkNodeInternal<?, PNode, ?> node,
													  final String label)
	{
		final var span = SubtreeSpanLocator.locate(source, node);
		if (span == null)
		{
			throw new InsertPlanException("Cannot locate " + label);
		}
		return span;
	}

	private static String indentSubtree(final String subtree, final String baseIndent)
	{
		if (baseIndent == null || baseIndent.isEmpty())
		{
			return subtree;
		}
		return subtree.replace("\n", "\n" + baseIndent);
	}

	private static String trailingIndentBefore(final CharSequence source, final int offset)
	{
		int start = offset;
		while (start > 0)
		{
			final char c = source.charAt(start - 1);
			if (c == ' ' || c == '\t')
			{
				start--;
				continue;
			}
			break;
		}
		return source.subSequence(start, offset).toString();
	}

	private static LinkNodeInternal<?, PNode, ?> resolveNode(final List<LinkNodeInternal<?, PNode, ?>> roots,
															 final String reference,
															 final String label)
	{
		final var resolution = new RootReferenceResolver().resolve(roots, reference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return found.node();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			throw new InsertPlanException("Ambiguous " + label + ": " + reference + " (" + ambiguous.candidates().size() + " matches)");
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			throw new InsertPlanException("Cannot resolve " + label + ": " + notFound.message());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			throw new InsertPlanException("Cannot resolve " + label + ": " + failure.message());
		}

		throw new InsertPlanException("Unexpected reference resolution state for " + label);
	}

	private static Relation<?, ?, ?, ?> resolveContainmentRelation(final LinkNodeInternal<?, PNode, ?> parentNode,
																   final String featureName)
	{
		final var group = parentNode.group();
		if (group == null)
		{
			return null;
		}

		return ModelUtil.streamAllFeatures(group)
						.filter(Relation.class::isInstance)
						.map(Relation.class::cast)
						.filter(Relation::contains)
						.filter(relation -> featureName.equals(relation.name()))
						.findFirst()
						.orElse(null);
	}

	private static ObjectId resolveObjectId(final LinkNodeInternal<?, PNode, ?> node, final String label)
	{
		try
		{
			final var built = node.build();
			final var id = ObjectId.from(built);
			if (id == null)
			{
				throw new InsertPlanException("Cannot resolve " + label + " object id");
			}
			return id;
		}
		catch (RuntimeException e)
		{
			throw new InsertPlanException("Cannot resolve " + label + " object id");
		}
	}

	private record PlanResult(InsertShiftContext shiftContext)
	{
	}

	private record InsertSlotReference(String parentReference, String featureName, OptionalInt index)
	{
		static InsertSlotReference parse(final String reference)
		{
			if (reference == null || reference.isBlank())
			{
				throw new InsertPlanException("Target reference is missing");
			}

			final int lastSlash = reference.lastIndexOf('/');
			if (lastSlash < 0)
			{
				throw new InsertPlanException("Target reference must include a parent path (for example '/parent/items.0')");
			}

			final var parentRef = lastSlash == 0 ? "/" : reference.substring(0, lastSlash);
			final var lastSegment = reference.substring(lastSlash + 1);

			final var index = parseIndex(lastSegment);
			final var featureName = index.isPresent()
									? lastSegment.substring(0, lastSegment.lastIndexOf('.'))
									: lastSegment;

			if (featureName.isBlank())
			{
				throw new InsertPlanException("Invalid target reference: " + reference);
			}

			return new InsertSlotReference(parentRef, featureName, index);
		}

		private static OptionalInt parseIndex(final String segment)
		{
			if (segment == null)
			{
				return OptionalInt.empty();
			}

			final int dot = segment.lastIndexOf('.');
			if (dot < 0 || dot == segment.length() - 1)
			{
				return OptionalInt.empty();
			}

			for (int i = dot + 1; i < segment.length(); i++)
			{
				if (!Character.isDigit(segment.charAt(i)))
				{
					return OptionalInt.empty();
				}
			}

			final int index = Integer.parseInt(segment.substring(dot + 1));
			return OptionalInt.of(index);
		}
	}

	private static Map<Path, List<TextEdits.TextEdit>> copyEdits(final Map<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		final var out = new HashMap<Path, List<TextEdits.TextEdit>>();
		for (final var entry : editsByFile.entrySet())
		{
			out.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Map.copyOf(out);
	}
}
