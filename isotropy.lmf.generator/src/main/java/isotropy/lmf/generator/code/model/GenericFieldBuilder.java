package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.util.*;

public final class GenericFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	public static final ClassName GENERIC_TYPE = ClassName.get(Generic.class);
	public static final ClassName GENERIC_IMPL_TYPE = ClassName.get(GenericImpl.class);
	public static final ClassName BT_TYPE = ClassName.get(BoundType.class);

	@Override
	public FieldSpec build(Group<?> input)
	{
		final var name = input.name();
		final var generycType = TypeParameter.of(GENERIC_TYPE, 1);
		final var typedList = TypeParameter.of(ConstantTypes.LIST, generycType.parametrizedWildcard());
		final var constantName = GenUtils.toConstantCase(name);
		final var initializerBuilder = CodeBlock.builder();

		final var genericBlockBuilder = new CodeblockBuilder<>(", ", GenericFieldBuilder::generateGenericsCodeblock);
		input.generics().forEach(genericBlockBuilder::feed);

		initializerBuilder.add("$T.of(", ConstantTypes.LIST).add(genericBlockBuilder.build()).add(")");

		return FieldSpec.builder(typedList.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Generic<?> generic)
	{
		final var type = generic.type();
		final var boundType = generic.boundType();

		final var btBlock = boundType == null ? CodeBlock.of("null") : CodeBlock.of("$T.$L", BT_TYPE, boundType);

		final var typeCodeblock = typeBlock(type);
		return CodeBlock.builder()
						.add("new $T<>($S, ", GENERIC_IMPL_TYPE, generic.name())
						.add(typeCodeblock)
						.add(", ")
						.add(btBlock)
						.add(")")
						.build();
	}

	private static CodeBlock typeBlock(Type<?> type)
	{
		if (type != null)
		{
			final var model = (Model) ModelUtils.root(type);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var typeConstantName = GenUtils.toConstantCase(type.name());
			final var typeHolder = TypeResolutionUtil.resolveTypeHolder(type);
			return CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeConstantName);
		}
		else
		{
			return CodeBlock.of("null");
		}
	}
}
