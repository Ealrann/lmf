package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new RelationBuilder<>();
  }

  Concept<UnaryType> concept();
  boolean lazy();
  boolean contains();

  interface Features<T extends Features<T>> extends Feature.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Boolean, Boolean> immutable = Feature.Features.immutable;
    RawFeature<Boolean, Boolean> many = Feature.Features.many;
    RawFeature<Boolean, Boolean> mandatory = Feature.Features.mandatory;
    RawFeature<GenericParameter, List<GenericParameter>> parameters = Feature.Features.parameters;
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = Feature.Features.rawFeature;
    RawFeature<Concept<?>, Concept<?>> concept = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.RELATION.CONCEPT);
    RawFeature<Boolean, Boolean> lazy = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.RELATION.LAZY);
    RawFeature<Boolean, Boolean> contains = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.RELATION.CONTAINS);
  }

  interface Builder<UnaryType extends LMObject, EffectiveType> extends IFeaturedObject.Builder<Relation<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);
    Builder<UnaryType, EffectiveType> concept(Supplier<Concept<UnaryType>> concept);
    Builder<UnaryType, EffectiveType> lazy(boolean lazy);
    Builder<UnaryType, EffectiveType> contains(boolean contains);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
