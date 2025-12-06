package org.logoce.lmf.model.lang;

import java.util.Optional;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.builder.MetaModelBuilder;

public final class LMCorePackage implements IModelPackage {
  public static final LMCorePackage Instance = new LMCorePackage();

  public static final MetaModel MODEL = new MetaModelBuilder().name("LMCore").domain("org.logoce.lmf.model").genNamePackage(false).extraPackage("lang").lmPackage(Instance).addGroups(LMCoreDefinition.Groups.ALL).addEnums(LMCoreDefinition.Enums.ALL).addUnits(LMCoreDefinition.Units.ALL).addAliases(LMCoreDefinition.Aliases.ALL).addJavaWrappers(LMCoreDefinition.JavaWrappers.ALL).build();

  private LMCorePackage() {
  }

  @Override
  public MetaModel model() {
    return MODEL;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group) {
    if (group == LMCoreDefinition.Groups.META_MODEL) return Optional.of((IFeaturedObject.Builder<T>) MetaModel.builder());
    else if (group == LMCoreDefinition.Groups.GROUP) return Optional.of((IFeaturedObject.Builder<T>) Group.builder());
    else if (group == LMCoreDefinition.Groups.INCLUDE) return Optional.of((IFeaturedObject.Builder<T>) Include.builder());
    else if (group == LMCoreDefinition.Groups.ATTRIBUTE) return Optional.of((IFeaturedObject.Builder<T>) Attribute.builder());
    else if (group == LMCoreDefinition.Groups.RELATION) return Optional.of((IFeaturedObject.Builder<T>) Relation.builder());
    else if (group == LMCoreDefinition.Groups.OPERATION) return Optional.of((IFeaturedObject.Builder<T>) Operation.builder());
    else if (group == LMCoreDefinition.Groups.OPERATION_PARAMETER) return Optional.of((IFeaturedObject.Builder<T>) OperationParameter.builder());
    else if (group == LMCoreDefinition.Groups.ALIAS) return Optional.of((IFeaturedObject.Builder<T>) Alias.builder());
    else if (group == LMCoreDefinition.Groups.ENUM) return Optional.of((IFeaturedObject.Builder<T>) Enum.builder());
    else if (group == LMCoreDefinition.Groups.UNIT) return Optional.of((IFeaturedObject.Builder<T>) Unit.builder());
    else if (group == LMCoreDefinition.Groups.GENERIC) return Optional.of((IFeaturedObject.Builder<T>) Generic.builder());
    else if (group == LMCoreDefinition.Groups.GENERIC_EXTENSION) return Optional.of((IFeaturedObject.Builder<T>) GenericExtension.builder());
    else if (group == LMCoreDefinition.Groups.GENERIC_PARAMETER) return Optional.of((IFeaturedObject.Builder<T>) GenericParameter.builder());
    else if (group == LMCoreDefinition.Groups.JAVA_WRAPPER) return Optional.of((IFeaturedObject.Builder<T>) JavaWrapper.builder());
    else if (group == LMCoreDefinition.Groups.SERIALIZER) return Optional.of((IFeaturedObject.Builder<T>) Serializer.builder());
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<T> resolveEnumLiteral(Enum<T> _enum, String value) {
    if (_enum == LMCoreDefinition.Enums.BOUND_TYPE) return (Optional<T>) Optional.of(BoundType.valueOf(value));
    else if (_enum == LMCoreDefinition.Enums.PRIMITIVE) return (Optional<T>) Optional.of(Primitive.valueOf(value));
    return Optional.empty();
  }
}
