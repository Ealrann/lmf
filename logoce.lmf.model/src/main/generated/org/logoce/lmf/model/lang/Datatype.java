package org.logoce.lmf.model.lang;

public interface Datatype<T> extends Type<T> {
  interface FeatureIDs<T extends FeatureIDs<T>> extends Type.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
  }
}
