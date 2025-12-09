package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.EnumBuilder;

public interface Enum<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new EnumBuilder<>();
  }

  List<String> literals();

  interface RFeatures<T extends RFeatures<T>> extends Datatype.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, List<String>> literals = new RawFeature<>(true,false,() -> Enum.Features.LITERALS);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int LITERALS = 384564756;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, List<String>> LITERALS = new AttributeBuilder<String, List<String>>().name("literals").immutable(true).many(true).rawFeature(Enum.RFeatures.literals).id(Enum.FeatureIDs.LITERALS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, LITERALS);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Enum<T>> {
    Builder<T> name(String name);
    Builder<T> addLiteral(String literal);
    Builder<T> addLiterals(List<String> literals);
  }
}
