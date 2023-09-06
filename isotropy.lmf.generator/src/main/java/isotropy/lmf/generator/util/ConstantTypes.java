package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.generator.code.util.FieldBuilder;

public class ConstantTypes
{
	public static final TypeParameter FEATURE = TypeParameter.of(ClassName.get(Feature.class), 2);
	public static final FieldBuilder.AllListBuilder FEATURE_ALL_BUILDER = new FieldBuilder.AllListBuilder(FEATURE);
	public static final TypeParameter GROUP = TypeParameter.of(ClassName.get(Group.class), 1);
	public static final FieldBuilder.AllListBuilder GROUP_ALL_BUILDER = new FieldBuilder.AllListBuilder(GROUP);
	public static final TypeParameter GENERIC = TypeParameter.of(ClassName.get(Generic.class), 1);
	public static final FieldBuilder.AllListBuilder GENERIC_ALL_BUILDER = new FieldBuilder.AllListBuilder(GENERIC);
	public static final TypeParameter UNIT = TypeParameter.of(ClassName.get(Unit.class), 1);
	public static final FieldBuilder.AllListBuilder UNIT_ALL_BUILDER = new FieldBuilder.AllListBuilder(UNIT);
	public static final TypeParameter ALIAS = TypeParameter.of(ClassName.get(Alias.class));
	public static final FieldBuilder.AllListBuilder ALIAS_ALL_BUILDER = new FieldBuilder.AllListBuilder(ALIAS);
	public static final TypeParameter ENUM = TypeParameter.of(ClassName.get(Enum.class), 1);
	public static final FieldBuilder.AllListBuilder ENUM_ALL_BUILDER = new FieldBuilder.AllListBuilder(ENUM);
	public static final TypeParameter JAVA_WRAPPER = TypeParameter.of(ClassName.get(JavaWrapper.class), 1);
	public static final FieldBuilder.AllListBuilder JAVA_WRAPPER_ALL_BUILDER = new FieldBuilder.AllListBuilder(
			JAVA_WRAPPER);
}
