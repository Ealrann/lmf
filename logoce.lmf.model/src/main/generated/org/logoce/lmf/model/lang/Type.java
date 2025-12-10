package org.logoce.lmf.model.lang;

public interface Type<T> extends Named {
  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
  }
}
