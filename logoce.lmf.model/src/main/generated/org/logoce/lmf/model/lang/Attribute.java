package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new AttributeBuilder<>();
  }

  Datatype<UnaryType> datatype();
  String defaultValue();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Feature.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = Feature.FeatureIDs.IMMUTABLE;
    int ID = Feature.FeatureIDs.ID;
    int MANY = Feature.FeatureIDs.MANY;
    int MANDATORY = Feature.FeatureIDs.MANDATORY;
    int PARAMETERS = Feature.FeatureIDs.PARAMETERS;
    int DATATYPE = -748350133;
    int DEFAULT_VALUE = 2061499287;
  }

  interface Builder<UnaryType, EffectiveType> extends IFeaturedObject.Builder<Attribute<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> id(int id);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> datatype);
    Builder<UnaryType, EffectiveType> defaultValue(String defaultValue);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
