package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.api.model.IModelPackage;
import isotropy.lmf.core.lang.impl.ModelImpl;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Optional;

public final class LMCorePackage implements IModelPackage {
  public static final LMCorePackage Instance = new LMCorePackage();

  public static final Model MODEL = new ModelImpl("LMCore", "isotropy.lmf.core.lang", LMCoreDefinition.Groups.ALL, LMCoreDefinition.Enums.ALL, LMCoreDefinition.Units.ALL, LMCoreDefinition.Aliases.ALL, LMCoreDefinition.JavaWrappers.ALL, Instance);

  private LMCorePackage() {
  }

  @Override
  public Model model() {
    return MODEL;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group) {
    if (group == LMCoreDefinition.Groups.MODEL) return Optional.of((IFeaturedObject.Builder<T>) Model.builder());
    else if (group == LMCoreDefinition.Groups.GROUP) return Optional.of((IFeaturedObject.Builder<T>) Group.builder());
    else if (group == LMCoreDefinition.Groups.ATTRIBUTE) return Optional.of((IFeaturedObject.Builder<T>) Attribute.builder());
    else if (group == LMCoreDefinition.Groups.RELATION) return Optional.of((IFeaturedObject.Builder<T>) Relation.builder());
    else if (group == LMCoreDefinition.Groups.ALIAS) return Optional.of((IFeaturedObject.Builder<T>) Alias.builder());
    else if (group == LMCoreDefinition.Groups.ENUM) return Optional.of((IFeaturedObject.Builder<T>) Enum.builder());
    else if (group == LMCoreDefinition.Groups.UNIT) return Optional.of((IFeaturedObject.Builder<T>) Unit.builder());
    else if (group == LMCoreDefinition.Groups.GENERIC) return Optional.of((IFeaturedObject.Builder<T>) Generic.builder());
    else if (group == LMCoreDefinition.Groups.REFERENCE) return Optional.of((IFeaturedObject.Builder<T>) Reference.builder());
    else if (group == LMCoreDefinition.Groups.JAVA_WRAPPER) return Optional.of((IFeaturedObject.Builder<T>) JavaWrapper.builder());
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
