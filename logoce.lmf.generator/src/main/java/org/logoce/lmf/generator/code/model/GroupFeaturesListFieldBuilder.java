package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtil;

import javax.lang.model.element.Modifier;

public final class GroupFeaturesListFieldBuilder implements CodeBuilder<Group<?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL};

	@Override
	public FieldSpec build(final Group<?> group)
	{
		final var model = (MetaModel) ModelUtil.root(group);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), group.name());
		final var constantName = GenUtils.toConstantCase(group.name());

		final var listType = TypeParameter.of(ConstantTypes.LIST, ConstantTypes.FEATURE.parametrizedWildcard());
		final var initializer = CodeBlock.builder().add("$T.of(", ConstantTypes.LIST);

		boolean first = true;
		for (final Feature<?, ?, ?, ?> feature : FeatureStreams.distinctFeatures(group).toList())
		{
			if (first) first = false;
			else initializer.add(", ");

			final var featureConstantName = GenUtils.toConstantCase(feature.name());
			initializer.add("$T.Features.$N", groupClass, featureConstantName);
		}

		initializer.add(")");

		return FieldSpec.builder(listType.parametrized(), constantName, modifiers)
						.initializer(initializer.build())
						.build();
	}
}
