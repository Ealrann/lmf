package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.lang.builder.UnitBuilder;

public final class UnitFieldBuilder implements DefinitionFieldBuilder<Unit<?>>
{
	public static final ClassName UNIT_TYPE = ClassName.get(Unit.class);
	public static final ClassName UNIT_BUILDER_TYPE = ClassName.get(UnitBuilder.class);

	@Override
	public FieldSpec build(Unit<?> input)
	{
		final var name = input.name();
		final var primitive = input.primitive();
		final var primitiveName = primitive.name();
		final var primitiveClass = GenUtils.resolvePrimitiveClass(primitive);
		final var primitiveClassName = TypeName.get(primitiveClass);
		final var typedUnit = TypeParameter.of(UNIT_TYPE, primitiveClassName.box());
		final var constantName = GenUtils.toConstantCase(name);
		final var initializer = CodeBlock.builder()
										 .add("new $T<$T>()", UNIT_BUILDER_TYPE, primitiveClassName.box())
										 .add(".name($S)", name)
										 .add(".matcher($S)", input.matcher())
										 .add(".defaultValue($S)", input.defaultValue())
										 .add(".primitive(Primitive.$N)", primitiveName)
										 .add(".extractor($S)", input.extractor())
										 .add(".build()")
										 .build();

		return FieldSpec.builder(typedUnit.parametrized(), constantName, modifiers)
						.initializer(initializer)
						.build();
	}
}
