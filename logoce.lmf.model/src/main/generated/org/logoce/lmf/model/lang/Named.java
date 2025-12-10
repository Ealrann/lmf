package org.logoce.lmf.model.lang;

public interface Named extends LMObject {
  String name();

  interface FeatureIDs<T extends FeatureIDs<T>> extends LMObject.FeatureIDs<T> {
    int NAME = 1328200565;
  }
}
