package org.logoce.lmf.model.lang;

import java.lang.String;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AliasBuilder;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  String value();

  interface Features extends Named.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> value = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.ALIAS.VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
