package org.logoce.lmf.core.loader.api.loader.model;

import org.logoce.lmf.core.lang.*;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.loader.feature.reference.PathParser;
import org.logoce.lmf.core.loader.feature.reference.PathUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.api.loader.util.ModelImports;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.logoce.lmf.core.loader.api.loader.model.LmSymbolIndex.ReferenceSpan;
import static org.logoce.lmf.core.loader.api.loader.model.LmSymbolIndex.SymbolId;
import static org.logoce.lmf.core.loader.api.loader.model.LmSymbolIndex.SymbolKind;
import static org.logoce.lmf.core.loader.api.loader.model.LmSymbolIndex.SymbolSpan;

/**
 * Legacy symbol index builder for a single document based on its parsed trees
 * and linked {@link Model}.
 * <p>
 * This implementation derives declarations from S-expression headers
 * (MetaModel, Group, Definition, Enum, Unit, JavaWrapper, Alias, Generic,
 * Operation, feature aliases) and collects references from {@code @Type} and
 * {@code #Model@Type} tokens using {@link PathUtil} and {@link ModelImports}.
 * <p>
 * New tooling should prefer {@link LmSemanticIndexBuilder}, which operates on
 * link trees and LMObjects instead of raw syntax. This builder is kept for
 * backwards compatibility and non-LSP tooling that still relies on
 * header-based indices.
 */
public final class LmSymbolIndexBuilder
{
	private LmSymbolIndexBuilder()
	{
	}

	public static LmSymbolIndex buildIndex(final Model model,
										   final List<Tree<PNode>> roots,
										   final CharSequence source,
										   final ModelRegistry registry)
	{
		if (model == null || roots.isEmpty())
		{
			return new LmSymbolIndex(List.of(), List.of());
		}

		final String modelDomain;
		final String modelName;
		if (model instanceof MetaModel mm)
		{
			modelDomain = mm.domain();
			modelName = mm.name();
		}
		else
		{
			modelDomain = "";
			modelName = model.name();
		}

		final var declarations = new ArrayList<SymbolSpan>();

		final var metaModelId = new SymbolId(modelDomain, modelName, SymbolKind.META_MODEL, modelName, "");

		for (final Tree<PNode> root : roots)
		{
			collectDeclarations(modelDomain, modelName, root, source, null, metaModelId, declarations);
		}

		final var references = new ArrayList<ReferenceSpan>();
		for (final Tree<PNode> root : roots)
		{
			collectReferences(root, modelDomain, modelName, model, source, registry, declarations, references);
		}

		return new LmSymbolIndex(List.copyOf(declarations), List.copyOf(references));
	}

	/**
	 * Build a symbol index for an instance-style (M1) document, where the
	 * active type environment is provided by a {@link MetaModel} but the
	 * document itself does not declare types in LMCore terms.
	 * <p>
	 * The returned index intentionally exposes only references:
	 * <ul>
	 *   <li>Declarations are always empty for the instance document.</li>
	 *   <li>References are collected from {@code @Type} and {@code #Model@Type}
	 *       tokens, resolved against {@code envMetaModel} and the registry.</li>
	 * </ul>
	 * This keeps type resolution semantics aligned between M1 and M2 while
	 * avoiding "fake" type declarations in instance documents.
	 */
	public static LmSymbolIndex buildIndexForInstance(final MetaModel envMetaModel,
													  final List<Tree<PNode>> roots,
													  final CharSequence source,
													  final ModelRegistry registry)
	{
		if (envMetaModel == null || roots == null || roots.isEmpty())
		{
			return new LmSymbolIndex(List.of(), List.of());
		}

		final String modelDomain = envMetaModel.domain();
		final String modelName = envMetaModel.name();

		final var references = new ArrayList<ReferenceSpan>();
		for (final Tree<PNode> root : roots)
		{
			collectInstanceReferences(root, modelDomain, modelName, envMetaModel, source, registry, references);
		}

		return new LmSymbolIndex(List.of(), List.copyOf(references));
	}

	private static void collectDeclarations(final String modelDomain,
											final String modelName,
											final Tree<PNode> node,
											final CharSequence source,
											final SymbolId currentContainer,
											final SymbolId metaModelId,
											final List<SymbolSpan> out)
	{
		final var pnode = node.data();
		final var tokens = pnode.tokens();
		final String head = tokens.isEmpty() ? "" : tokens.getFirst().value();

		final SymbolKind kind = kindForHead(head);
		SymbolId container = currentContainer;

		if (kind != null)
		{
			final Optional<PToken> nameToken = resolveNameToken(tokens);
			final String symbolName = nameToken.map(PToken::value).orElseGet(() -> head != null ? head : "");

			if (!symbolName.isEmpty())
			{
				final var span = nameToken.map(tok -> TextPositions.spanOf(tok, source))
										  .orElseGet(() -> TextPositions.spanOf(pnode, source));
				final var id = new SymbolId(modelDomain, modelName, kind, symbolName, "");

				if (kind == SymbolKind.META_MODEL)
				{
					container = null;
				}
				else if (kind == SymbolKind.TYPE)
				{
					container = metaModelId;
				}

				out.add(new SymbolSpan(id, span, container));

				if (kind == SymbolKind.META_MODEL || kind == SymbolKind.TYPE)
				{
					// This symbol becomes the container for its children.
					container = id;
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			collectDeclarations(modelDomain, modelName, child, source, container, metaModelId, out);
		}
	}

	private static void collectReferences(final Tree<PNode> node,
										  final String modelDomain,
										  final String modelName,
										  final Model currentModel,
										  final CharSequence source,
										  final ModelRegistry registry,
										  final List<SymbolSpan> declarations,
										  final List<ReferenceSpan> out)
	{
		final var tokens = node.data().tokens();
		for (final PToken token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isEmpty())
			{
				continue;
			}
			final char first = value.charAt(0);
			if (first == '@')
			{
				final String typeName = value.substring(1);
				if (!typeName.isEmpty())
				{
					final var id = new SymbolId(modelDomain, modelName, SymbolKind.TYPE, typeName, "");
					if (containsDeclaration(declarations, id))
					{
						final var span = TextPositions.spanOf(token, source);
						out.add(new ReferenceSpan(id, span));
					}
				}
			}
			else if (first == '#')
			{
				final var parsed = PathUtil.parse(value);
				String targetModelName = null;
				String targetType = null;

				for (final var segment : parsed.segments())
				{
					if (segment.type() == PathParser.Type.MODEL && targetModelName == null)
					{
						targetModelName = segment.text();
					}
					else if (segment.type() == PathParser.Type.NAME)
					{
						targetType = segment.text();
					}
				}

				if (targetModelName != null && targetType != null)
				{
					final var resolved = ModelImports.resolveModel(currentModel, targetModelName, registry);
					final String targetDomain;
					final String targetModel;
					if (resolved.isPresent() && resolved.get() instanceof MetaModel mm)
					{
						targetDomain = mm.domain();
						targetModel = mm.name();
					}
					else
					{
						targetDomain = "";
						targetModel = targetModelName;
					}

					final var id = new SymbolId(targetDomain, targetModel, SymbolKind.TYPE, targetType, "");
					if (containsDeclaration(declarations, id))
					{
						final var span = TextPositions.spanOf(token, source);
						out.add(new ReferenceSpan(id, span));
					}
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			collectReferences(child, modelDomain, modelName, currentModel, source, registry, declarations, out);
		}
	}

	private static void collectInstanceReferences(final Tree<PNode> node,
												  final String envDomain,
												  final String envName,
												  final MetaModel envMetaModel,
												  final CharSequence source,
												  final ModelRegistry registry,
												  final List<ReferenceSpan> out)
	{
		final var tokens = node.data().tokens();
		for (final PToken token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isEmpty())
			{
				continue;
			}

			final char first = value.charAt(0);
			if (first == '@')
			{
				final String typeName = value.substring(1);
				if (!typeName.isEmpty() && envHasType(envMetaModel, typeName))
				{
					final var id = new SymbolId(envDomain, envName, SymbolKind.TYPE, typeName, "");
					final var span = TextPositions.spanOf(token, source);
					out.add(new ReferenceSpan(id, span));
				}
			}
			else if (first == '#')
			{
				final var parsed = PathUtil.parse(value);
				String targetModelName = null;
				String targetType = null;

				for (final var segment : parsed.segments())
				{
					if (segment.type() == PathParser.Type.MODEL && targetModelName == null)
					{
						targetModelName = segment.text();
					}
					else if (segment.type() == PathParser.Type.NAME)
					{
						targetType = segment.text();
					}
				}

				if (targetModelName != null && targetType != null)
				{
					final var resolved = ModelImports.resolveModel(envMetaModel, targetModelName, registry);
					final String targetDomain;
					final String targetModel;

					if (resolved.isPresent() && resolved.get() instanceof MetaModel mm)
					{
						targetDomain = mm.domain();
						targetModel = mm.name();
					}
					else
					{
						targetDomain = "";
						targetModel = targetModelName;
					}

					final MetaModel targetMetaModel = resolved.orElse(envMetaModel) instanceof MetaModel mm
													  ? mm
													  : null;
					if (targetMetaModel != null && envHasType(targetMetaModel, targetType))
					{
						final var id = new SymbolId(targetDomain, targetModel, SymbolKind.TYPE, targetType, "");
						final var span = TextPositions.spanOf(token, source);
						out.add(new ReferenceSpan(id, span));
					}
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			collectInstanceReferences(child, envDomain, envName, envMetaModel, source, registry, out);
		}
	}

	private static boolean containsDeclaration(final List<SymbolSpan> declarations, final SymbolId id)
	{
		for (final var decl : declarations)
		{
			if (decl.id().equals(id))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean envHasType(final MetaModel mm, final String typeName)
	{
		if (typeName == null || typeName.isEmpty())
		{
			return false;
		}

		for (final Group<?> g : mm.groups())
		{
			if (typeName.equals(g.name()))
			{
				return true;
			}
		}
		for (final Enum<?> e : mm.enums())
		{
			if (typeName.equals(e.name()))
			{
				return true;
			}
		}
		for (final Unit<?> u : mm.units())
		{
			if (typeName.equals(u.name()))
			{
				return true;
			}
		}
		for (final JavaWrapper<?> w : mm.javaWrappers())
		{
			if (typeName.equals(w.name()))
			{
				return true;
			}
		}
		return false;
	}

	private static SymbolKind kindForHead(final String head)
	{
		if (head == null) return null;
		final String trimmed = head.trim();

		return switch (trimmed)
		{
			case "MetaModel" -> SymbolKind.META_MODEL;
			case "Group", "Definition", "Enum", "Unit", "JavaWrapper" -> SymbolKind.TYPE;
			case "Alias", "Generic", "Operation" -> SymbolKind.FEATURE;
			default ->
			{
				if (trimmed.startsWith("+") || trimmed.startsWith("-") || "reference".equals(trimmed))
				{
					yield SymbolKind.FEATURE;
				}
				yield null;
			}
		};
	}

	private static Optional<PToken> resolveNameToken(final List<PToken> tokens)
	{
		for (int i = 1; i < tokens.size(); i++)
		{
			final PToken tok = tokens.get(i);
			final String val = tok.value();

			if (val.startsWith("name=") && val.length() > "name=".length())
			{
				return Optional.of(new PToken(val.substring("name=".length()), tok.type(), tok.offset(), tok.length()));
			}

			if ("name".equals(val))
			{
				if (i + 2 < tokens.size() && "=".equals(tokens.get(i + 1).value()))
				{
					final PToken candidate = tokens.get(i + 2);
					return Optional.of(new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length()));
				}
				if (i + 1 < tokens.size())
				{
					final PToken candidate = tokens.get(i + 1);
					return Optional.of(new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length()));
				}
			}

			final int eq = val.indexOf('=');
			if (eq > 0 && eq + 1 < val.length())
			{
				return Optional.of(new PToken(val.substring(eq + 1), tok.type(), tok.offset() + eq + 1, tok.length() - eq - 1));
			}
		}

		for (int i = 1; i < tokens.size(); i++)
		{
			final PToken tok = tokens.get(i);
			final String val = tok.value();
			if (val.isEmpty()) continue;
			if (!Character.isJavaIdentifierStart(val.charAt(0))) continue;
			if (val.startsWith("+") || val.startsWith("-")) continue;
			if (val.contains("=")) continue;
			if ("MetaModel".equals(val) ||
				"Group".equals(val) ||
				"Definition".equals(val) ||
				"Enum".equals(val) ||
				"Unit".equals(val) ||
				"Generic".equals(val) ||
				"Alias".equals(val) ||
				"JavaWrapper".equals(val) ||
				"includes".equals(val))
			{
				continue;
			}
			return Optional.of(tok);
		}

		return Optional.empty();
	}
}
