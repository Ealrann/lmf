package org.logoce.lmf.core.lang;

import java.util.Optional;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IJavaWrapperConverter;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.builder.MetaModelBuilder;

public final class LMCoreModelPackage implements IModelPackage {
  public static final LMCoreModelPackage Instance = new LMCoreModelPackage();

  public static final MetaModel MODEL = new MetaModelBuilder().name("LMCore").domain("org.logoce.lmf.core").genNamePackage(false).extraPackage("lang").lmPackage(Instance).addGroups(LMCoreModelDefinition.Groups.ALL).addEnums(LMCoreModelDefinition.Enums.ALL).addUnits(LMCoreModelDefinition.Units.ALL).addAliases(LMCoreModelDefinition.Aliases.ALL).addJavaWrappers(LMCoreModelDefinition.JavaWrappers.ALL).build();

  private LMCoreModelPackage() {
  }

  @Override
  public MetaModel model() {
    return MODEL;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group) {
    if (group == LMCoreModelDefinition.Groups.META_MODEL) return Optional.of((IFeaturedObject.Builder<T>) MetaModel.builder());
    else if (group == LMCoreModelDefinition.Groups.GROUP) return Optional.of((IFeaturedObject.Builder<T>) Group.builder());
    else if (group == LMCoreModelDefinition.Groups.INCLUDE) return Optional.of((IFeaturedObject.Builder<T>) Include.builder());
    else if (group == LMCoreModelDefinition.Groups.ATTRIBUTE) return Optional.of((IFeaturedObject.Builder<T>) Attribute.builder());
    else if (group == LMCoreModelDefinition.Groups.RELATION) return Optional.of((IFeaturedObject.Builder<T>) Relation.builder());
    else if (group == LMCoreModelDefinition.Groups.OPERATION) return Optional.of((IFeaturedObject.Builder<T>) Operation.builder());
    else if (group == LMCoreModelDefinition.Groups.OPERATION_PARAMETER) return Optional.of((IFeaturedObject.Builder<T>) OperationParameter.builder());
    else if (group == LMCoreModelDefinition.Groups.ALIAS) return Optional.of((IFeaturedObject.Builder<T>) Alias.builder());
    else if (group == LMCoreModelDefinition.Groups.ENUM) return Optional.of((IFeaturedObject.Builder<T>) Enum.builder());
    else if (group == LMCoreModelDefinition.Groups.ENUM_ATTRIBUTE) return Optional.of((IFeaturedObject.Builder<T>) EnumAttribute.builder());
    else if (group == LMCoreModelDefinition.Groups.UNIT) return Optional.of((IFeaturedObject.Builder<T>) Unit.builder());
    else if (group == LMCoreModelDefinition.Groups.GENERIC) return Optional.of((IFeaturedObject.Builder<T>) Generic.builder());
    else if (group == LMCoreModelDefinition.Groups.GENERIC_EXTENSION) return Optional.of((IFeaturedObject.Builder<T>) GenericExtension.builder());
    else if (group == LMCoreModelDefinition.Groups.GENERIC_PARAMETER) return Optional.of((IFeaturedObject.Builder<T>) GenericParameter.builder());
    else if (group == LMCoreModelDefinition.Groups.JAVA_WRAPPER) return Optional.of((IFeaturedObject.Builder<T>) JavaWrapper.builder());
    else if (group == LMCoreModelDefinition.Groups.SERIALIZER) return Optional.of((IFeaturedObject.Builder<T>) Serializer.builder());
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<T> resolveEnumLiteral(Enum<T> enum_, String value) {
    if (enum_ == LMCoreModelDefinition.Enums.BOUND_TYPE) return (Optional<T>) Optional.of(BoundType.valueOf(value));
    else if (enum_ == LMCoreModelDefinition.Enums.PRIMITIVE) return (Optional<T>) Optional.of(Primitive.valueOf(value));
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<IJavaWrapperConverter<T>> resolveJavaWrapperConverter(
      JavaWrapper<T> wrapper) {
    return Optional.empty();
  }
}
