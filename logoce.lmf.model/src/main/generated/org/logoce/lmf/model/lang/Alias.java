package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AliasBuilder;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  String value();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int VALUE = -1357955170;
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
