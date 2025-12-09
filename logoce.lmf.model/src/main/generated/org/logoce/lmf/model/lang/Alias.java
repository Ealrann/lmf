package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AliasBuilder;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  String value();

  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> value = new RawFeature<>(false,false,() -> Alias.Features.VALUE);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int VALUE = -1357955170;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> VALUE = new AttributeBuilder<String, String>().name("value").immutable(true).mandatory(true).rawFeature(Alias.RFeatures.value).id(Alias.FeatureIDs.VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
