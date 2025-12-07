package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AliasBuilder;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  String value();

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> value = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.ALIAS.VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
