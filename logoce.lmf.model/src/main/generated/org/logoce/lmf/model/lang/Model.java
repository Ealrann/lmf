package org.logoce.lmf.model.lang;

import java.util.List;

public interface Model extends Named {
  String domain();
  List<String> imports();
  List<String> metamodels();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int DOMAIN = -463269570;
    int IMPORTS = -1387965388;
    int METAMODELS = -1744988119;
  }
}
