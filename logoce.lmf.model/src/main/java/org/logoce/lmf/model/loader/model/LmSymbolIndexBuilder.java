package org.logoce.lmf.model.loader.model;

import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.linking.feature.reference.PathParser;
import org.logoce.lmf.model.loader.linking.feature.reference.PathUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.ModelImports;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.logoce.lmf.model.loader.model.LmSymbolIndex.ReferenceSpan;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolId;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolKind;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolSpan;

/**
 * Builds an {@link LmSymbolIndex} for a single document based purely on its
 * parsed trees and linked {@link Model}. Intended for tooling (LSP, editor)
 * as a shared, language-level index.
 * <p>
 * The current implementation mirrors the existing LSP behaviour:
 * <ul>
 *   <li>Declarations are derived from S-expression headers (MetaModel, Group, Definition,
 *       Enum, Unit, JavaWrapper, Alias, Generic, Operation, feature aliases).</li>
 *   <li>References are collected from {@code @Type} and {@code #Model@Type} tokens,
 *       using {@link PathUtil} and {@link ModelImports} for cross-model resolution.</li>
 * </ul>
 * This keeps symbol and reference semantics in {@code logoce.lmf.model} while
 * letting front-ends translate them into UI-specific structures.
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

		final var metaModelId = new SymbolId(modelDomain, modelName, SymbolKind.META_MODEL, modelName);

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
				final var id = new SymbolId(modelDomain, modelName, kind, symbolName);

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
					final var id = new SymbolId(modelDomain, modelName, SymbolKind.TYPE, typeName);
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

					final var id = new SymbolId(targetDomain, targetModel, SymbolKind.TYPE, targetType);
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
