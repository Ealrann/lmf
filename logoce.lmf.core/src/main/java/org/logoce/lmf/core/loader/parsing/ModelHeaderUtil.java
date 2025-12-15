package org.logoce.lmf.core.loader.parsing;

import org.logoce.lmf.core.api.lexer.ELMTokenType;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for reading model-level header properties from a parsed LM tree.
 * <p>
 * These helpers operate on the raw {@link PNode} representation and are shared
 * between the loader/linker stack and tooling integrations (e.g. the LSP).
 */
public final class ModelHeaderUtil
{
	private ModelHeaderUtil()
	{
	}

	/**
	 * Returns {@code true} if the first root node represents a MetaModel header.
	 */
	public static boolean isMetaModelRoot(final List<? extends Tree<PNode>> roots)
	{
		if (roots.isEmpty())
		{
			return false;
		}

		final var tokens = roots.getFirst().data().tokens();
		for (final var token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}
			return "MetaModel".equals(value);
		}
		return false;
	}

	/**
	 * Resolve the {@code domain} header property, or {@code null} if it is absent.
	 */
	public static String resolveDomain(final PNode node)
	{
		return extractValue(node, "domain");
	}

	/**
	 * Resolve the {@code name} header property.
	 *
	 * @throws IllegalStateException if the name is not present
	 */
	public static String resolveName(final PNode node)
	{
		final var name = extractValue(node, "name");
		if (name == null)
		{
			throw new IllegalStateException("Model name should be set");
		}
		return name;
	}

	/**
	 * Resolve the {@code metamodels} header property as a list of qualified model names.
	 */
	public static java.util.List<String> resolveMetamodelNames(final PNode node)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && "metamodels".equals(token.value()))
			{
				if (it.hasNext()) it.next(); // skip ASSIGN

				final var values = new java.util.ArrayList<String>();
				while (it.hasNext())
				{
					final var next = it.next();
					if (next.type() == ELMTokenType.VALUE)
					{
						for (final var part : next.value().split(","))
						{
							final var trimmed = part.trim();
							if (!trimmed.isEmpty())
							{
								values.add(trimmed);
							}
						}
					}
					else if (next.type() != ELMTokenType.LIST_SEPARATOR && next.type() != ELMTokenType.WHITE_SPACE)
					{
						break;
					}
				}
				return values;
			}
		}
		return java.util.List.of();
	}

	/**
	 * Resolve the {@code imports} header property as a list of qualified model names.
	 */
	public static List<String> resolveImports(final PNode node)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && "imports".equals(token.value()))
			{
				if (it.hasNext()) it.next(); // skip ASSIGN

				final var values = new ArrayList<String>();
				while (it.hasNext())
				{
					final var next = it.next();
					if (next.type() == ELMTokenType.VALUE)
					{
						for (final var part : next.value().split(","))
						{
							final var trimmed = part.trim();
							if (!trimmed.isEmpty())
							{
								values.add(trimmed);
							}
						}
					}
					else if (next.type() != ELMTokenType.LIST_SEPARATOR && next.type() != ELMTokenType.WHITE_SPACE)
					{
						break;
					}
				}
				return values;
			}
		}
		return List.of();
	}

	private static String extractValue(final PNode node, final String property)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && property.equals(token.value()))
			{
				if (it.hasNext()) it.next(); // skip ASSIGN
				if (it.hasNext())
				{
					return it.next().value();
				}
				break;
			}
		}
		return null;
	}
}
