package org.logoce.lmf.generator.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.core.lang.*;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.generator.code.util.FieldBuilder;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.lang.builder.MetaModelBuilder;
import org.logoce.lmf.core.api.notification.list.ObservableList;
import org.logoce.lmf.core.util.BuildUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConstantTypes
{
	public static final ClassName LIST = ClassName.get(List.class);
	public static final TypeParameter FEATURE = TypeParameter.of(ClassName.get(Feature.class), 4);
	public static final TypeParameter GROUP = TypeParameter.of(ClassName.get(Group.class), 1);
	public static final TypeParameter GENERIC = TypeParameter.of(ClassName.get(Generic.class), 1);
	public static final TypeParameter UNIT = TypeParameter.of(ClassName.get(Unit.class), 1);
	public static final TypeParameter ALIAS = TypeParameter.of(ClassName.get(Alias.class));
	public static final TypeParameter ENUM = TypeParameter.of(ClassName.get(Enum.class), 1);
	public static final TypeParameter JAVA_WRAPPER = TypeParameter.of(ClassName.get(JavaWrapper.class), 1);

	public static final FieldBuilder.AllListBuilder FEATURE_ALL_BUILDER = new FieldBuilder.AllListBuilder(FEATURE);
	public static final FieldBuilder.AllListBuilder GROUP_ALL_BUILDER = new FieldBuilder.AllListBuilder(GROUP);
	public static final FieldBuilder.AllListBuilder GENERIC_ALL_BUILDER = new FieldBuilder.AllListBuilder(GENERIC);
	public static final FieldBuilder.AllListBuilder UNIT_ALL_BUILDER = new FieldBuilder.AllListBuilder(UNIT);
	public static final FieldBuilder.AllListBuilder ALIAS_ALL_BUILDER = new FieldBuilder.AllListBuilder(ALIAS);
	public static final FieldBuilder.AllListBuilder ENUM_ALL_BUILDER = new FieldBuilder.AllListBuilder(ENUM);
	public static final FieldBuilder.AllListBuilder JAVA_WRAPPER_ALL_BUILDER = new FieldBuilder.AllListBuilder(
			JAVA_WRAPPER);
	public static final ClassName FEATURE_INSERTER_CLASS = ClassName.get(FeatureInserter.class);
	public static final TypeName ARRAYLIST = ClassName.get(ArrayList.class);
	public static final ClassName OBSERVABLE_LIST = ClassName.get(ObservableList.class);
	public static final ClassName STREAM = ClassName.get(Stream.class);
	public static final ClassName FEATURED_OBJECT_BUILDER = ClassName.get(IFeaturedObject.Builder.class);
	public static final ClassName SUPPLIER = ClassName.get(Supplier.class);
	public static final ClassName OPTIONAL = ClassName.get(Optional.class);
	public static final ClassName IMODEL_PACKAGE = ClassName.get(IModelPackage.class);
	public static final ClassName MODEL = ClassName.get(MetaModel.class);
	public static final ClassName META_MODEL_BUILDER = ClassName.get(MetaModelBuilder.class);
	public static final ClassName STRING = ClassName.get(String.class);
	public static final ClassName LM_OBJECT = ClassName.get(LMObject.class);
	public static final AnnotationSpec SUPPRESS_UNCHECKED = AnnotationSpec.builder(SuppressWarnings.class)
																		  .addMember("value", "\"unchecked\"")
																		  .build();
	public static final AnnotationSpec SUPPRESS_RAW_UNCHECKED = AnnotationSpec.builder(SuppressWarnings.class)
																			  .addMember("value", "\"unchecked\"")
																			  .addMember("value", "\"rawtypes\"")
																			  .build();
	public static final AnnotationSpec OVERRIDE = AnnotationSpec.builder(Override.class).build();
	public static final ClassName BUILD_UTILS = ClassName.get(BuildUtils.class);
}
