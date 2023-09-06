package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.lang.impl.UnitImpl;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public final class UnitFieldBuilder implements CodeBuilder<Unit<?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
	public static final ClassName UNIT_TYPE = ClassName.get(Unit.class);
	public static final ClassName UNIT_IMPL_TYPE = ClassName.get(UnitImpl.class);

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

		return FieldSpec.builder(typedUnit.parametrized(), constantName, modifiers)
						.initializer("new $T<>($S, $S, $S, Primitive.$N, $S)",
									 UNIT_IMPL_TYPE,
									 name,
									 input.matcher(),
									 input.defaultValue(),
									 primitiveName,
									 input.extractor())
						.build();
	}
}
