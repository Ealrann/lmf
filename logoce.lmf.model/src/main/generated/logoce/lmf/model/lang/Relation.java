package logoce.lmf.model.lang;

import java.lang.Boolean;
import java.lang.String;
import java.util.function.Supplier;
import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.model.IFeaturedObject;
import logoce.lmf.model.lang.builder.RelationBuilder;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new RelationBuilder<>();
  }

  Reference<UnaryType> reference();

  boolean lazy();

  boolean contains();

  interface Features extends Feature.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<Boolean, Boolean> immutable = Feature.Features.immutable;

    RawFeature<Boolean, Boolean> many = Feature.Features.many;

    RawFeature<Boolean, Boolean> mandatory = Feature.Features.mandatory;

    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = Feature.Features.rawFeature;

    RawFeature<Reference<?>, Reference<?>> reference = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.RELATION.REFERENCE);

    RawFeature<Boolean, Boolean> lazy = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.RELATION.LAZY);

    RawFeature<Boolean, Boolean> contains = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.RELATION.CONTAINS);
  }

  interface Builder<UnaryType extends LMObject, EffectiveType> extends IFeaturedObject.Builder<Relation<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);

    Builder<UnaryType, EffectiveType> immutable(boolean immutable);

    Builder<UnaryType, EffectiveType> many(boolean many);

    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);

    Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);

    Builder<UnaryType, EffectiveType> reference(Supplier<Reference<UnaryType>> reference);

    Builder<UnaryType, EffectiveType> lazy(boolean lazy);

    Builder<UnaryType, EffectiveType> contains(boolean contains);
  }
}
