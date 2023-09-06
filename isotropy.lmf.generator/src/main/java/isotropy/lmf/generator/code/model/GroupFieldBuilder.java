package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GroupImpl;
import isotropy.lmf.core.lang.impl.ReferenceImpl;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.CodeblockBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;

public final class GroupFieldBuilder implements CodeBuilder<Group<?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
	public static final ClassName GROUP_TYPE = ClassName.get(Group.class);
	public static final ClassName GROUP_IMPL_TYPE = ClassName.get(GroupImpl.class);
	public static final ClassName REFERENCE_IMPL_TYPE = ClassName.get(ReferenceImpl.class);
	public static final ClassName LIST_TYPE = ClassName.get(List.class);

	@Override
	public FieldSpec build(Group<?> input)
	{
		final var name = input.name();
		final var type = TypeParameter.of(ClassName.get("", input.name()), input.generics().size());
		final var typedGroup = TypeParameter.of(GROUP_TYPE, type.parametrizedWildcard());
		final var constantName = GenUtils.toConstantCase(name);
		final var initializerBuilder = CodeBlock.builder();

		final var referenceBlockBuilder = new CodeblockBuilder<>(", ", GroupFieldBuilder::generateReferencesCodeblock);
		input.includes().forEach(referenceBlockBuilder::feed);

		final var genericBlock = input.generics().isEmpty()
								 ? CodeBlock.of("$T.of()", LIST_TYPE)
								 : CodeBlock.of("Generics.$N", constantName);

		initializerBuilder.add("new $T<>(", GROUP_IMPL_TYPE)
						  .add("$S, $L, ", name, input.concrete())
						  .add("$T.of(", LIST_TYPE)
						  .add(referenceBlockBuilder.build())
						  .add("), Features.$N.ALL,", constantName)
						  .add(genericBlock)
						  .add(")");

		return FieldSpec.builder(typedGroup.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	public static CodeBlock generateReferencesCodeblock(final Reference<?> reference)
	{
		final var genericsBlockBuilder = new CodeblockBuilder<>(", ", GroupFieldBuilder::generateGenericsCodeblock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		reference.parameters().forEach(genericsBlockBuilder::feed);

		return CodeBlock.builder()
						.add("new $T<>(() -> $N, ", REFERENCE_IMPL_TYPE, groupConstantName)
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
