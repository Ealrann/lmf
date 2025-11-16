package org.logoce.lmf.editor.highlight.proposition;

import org.jetbrains.annotations.NotNull;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.util.MetaModelRegistry;

import java.util.List;
import java.util.stream.Stream;

public final class AttributeComputer
{
	private final List<Attribute<?, ?>> attributes;
	private final boolean withDefaultValue;

	public AttributeComputer(final List<Attribute<?, ?>> attributes, final boolean withDefaultValue)
	{
		this.attributes = attributes;
		this.withDefaultValue = withDefaultValue;
	}

	public Stream<String> computeEntries()
	{
		return attributes.stream().flatMap(this::streamProposals);
	}

	@NotNull
	private Stream<String> streamProposals(final Attribute<?, ?> attribute)
	{
		final var attributeName = attribute.name();

		final var aliases = MetaModelRegistry.Instance.metamodels()
													  .flatMap(m -> m.model().aliases().stream())
													  .filter(alias -> alias.value().startsWith(attributeName))
													  .map(Named::name);

		final var value = withDefaultValue ? attributeName + "=" + attribute.defaultValue() : attributeName;
		return Stream.concat(Stream.of(value), aliases);
	}
}
