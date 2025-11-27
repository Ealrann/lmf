package org.logoce.lmf.model.lang;

import java.lang.Boolean;
import java.lang.String;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new AttributeBuilder<>();
  }

  Datatype<UnaryType> datatype();
  String defaultValue();

  interface Features<T extends Features<T>> extends Feature.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Boolean, Boolean> immutable = Feature.Features.immutable;
    RawFeature<Boolean, Boolean> many = Feature.Features.many;
    RawFeature<Boolean, Boolean> mandatory = Feature.Features.mandatory;
    RawFeature<GenericParameter, List<GenericParameter>> parameters = Feature.Features.parameters;
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = Feature.Features.rawFeature;
    RawFeature<Datatype<?>, Datatype<?>> datatype = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.ATTRIBUTE.DATATYPE);
    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.ATTRIBUTE.DEFAULT_VALUE);
  }

  interface Builder<UnaryType, EffectiveType> extends IFeaturedObject.Builder<Attribute<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);
    Builder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> datatype);
    Builder<UnaryType, EffectiveType> defaultValue(String defaultValue);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
