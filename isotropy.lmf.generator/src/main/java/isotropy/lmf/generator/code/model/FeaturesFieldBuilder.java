package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.ReferenceImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.CodeblockBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;
import isotropy.lmf.generator.util.TypeResolutionUtil;

import javax.lang.model.element.Modifier;
import java.util.List;

public final class FeaturesFieldBuilder implements CodeBuilder<Feature<?, ?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
	public static final ClassName ATTRIBUTE_TYPE = ClassName.get(Attribute.class);
	public static final ClassName ATTRIBUTE_IMPL_TYPE = ClassName.get(AttributeImpl.class);
	public static final ClassName RELATION_TYPE = ClassName.get(Relation.class);
	public static final ClassName RELATION_IMPL_TYPE = ClassName.get(RelationImpl.class);
	public static final ClassName REFERENCE_IMPL_TYPE = ClassName.get(ReferenceImpl.class);
	public static final ClassName LIST_TYPE = ClassName.get(List.class);

	private final Group<?> group;

	public FeaturesFieldBuilder(Group<?> group)
	{
		this.group = group;
	}

	@Override
	public FieldSpec build(Feature<?, ?> input)
	{
		final var name = input.name();
		final var parentGroup = (Group<?>) input.lmContainer();
		final var constantName = GenUtils.toConstantCase(name);
		final var type = TypeResolutionUtil.resolveType(input);
		final var effectiveType = TypeResolutionUtil.effectiveType(input, type);

		final var types = List.of(type.parametrizedWildcard(), effectiveType.parametrizedWildcard());
		final var isAttribute = input instanceof Attribute<?, ?>;
		final var mainType = isAttribute
							 ? TypeParameter.of(ATTRIBUTE_TYPE, types)
							 : TypeParameter.of(RELATION_TYPE, types);

		final var initBuilder = CodeBlock.builder();

		if (group != parentGroup)
		{
			initBuilder.add(parentInitializer(input));
		}
		else
		{
			initBuilder.add("new $T<>(", isAttribute ? ATTRIBUTE_IMPL_TYPE : RELATION_IMPL_TYPE)
					   .add("$S, ", name)
					   .add("$L, ", input.immutable())
					   .add("$L, ", input.many())
					   .add("$L, ", input.mandatory());

			if (isAttribute)
			{
				final var attribute = (Attribute<?, ?>) input;
				final var datatype = attribute.datatype();
				final var typeHolder = TypeResolutionUtil.resolveTypeHolder(datatype);
				final var typeName = GenUtils.toConstantCase(datatype.name());

				initBuilder.add("$N.$N, ", typeHolder, typeName)
						   .add("$T.of(), ", LIST_TYPE)
						   .add("$N.Features.$N", parentGroup.name(), name);
			}
			else
			{
				final var relation = (Relation<?, ?>) input;
				final var reference = relation.reference();
				final var refBlock = generateReferencesCodeblock(reference);

				initBuilder.add(refBlock)
						   .add(", $L, ", relation.contains())
						   .add("$N.Features.$N", parentGroup.name(), name);
			}

			initBuilder.add(")");
		}

		return FieldSpec.builder(mainType.parametrized(), constantName, modifiers)
						.initializer(initBuilder.build())
						.build();
	}

	private static CodeBlock parentInitializer(final Feature<?, ?> feature)
	{
		final var group = (Group<?>) feature.lmContainer();
		final var model = (Model) group.lmContainer();
		final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
		final var constantGroupName = GenUtils.toConstantCase(group.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.Features.$N.$N", modelDefinition, constantGroupName, constantFeatureName);
	}

	public static CodeBlock generateReferencesCodeblock(final Reference<?> reference)
	{
		final var genericsBlockBuilder = new CodeblockBuilder<>(", ", FeaturesFieldBuilder::generateGenericsCodeblock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		final var conceptHolder = TypeResolutionUtil.resolveConceptHolder(group);
		reference.parameters().forEach(genericsBlockBuilder::feed);

		return CodeBlock.builder()
						.add("new $T<>(() -> $N.$N, ", REFERENCE_IMPL_TYPE, conceptHolder, groupConstantName)
						.add("$T.of(", LIST_TYPE)
						.add(genericsBlockBuilder.build())
						.add("))")
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Concept<?> concept)
	{
		if (concept instanceof Generic<?> generic)
		{
			final var group = (Group<?>) generic.lmContainer();
			final var model = (Model) ModelUtils.root(group);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var constantName = GenUtils.toConstantCase(group.name());
			final var index = group.generics().indexOf(generic);

			return CodeBlock.builder()
							.add("() -> $T.Generics.$N.get($L)", modelDefinition, constantName, index)
							.build();
		}
		else
		{
			final var group = (Group<?>) concept;
			final var model = (Model) ModelUtils.root(group);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var constantName = GenUtils.toConstantCase(group.name());

			return CodeBlock.builder().add("() -> $T.Groups.$N", modelDefinition, constantName).build();
		}
	}
}
