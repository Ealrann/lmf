package org.logoce.lmf.model.lang;

import java.util.List;

public interface Feature<UnaryType, EffectiveType> extends Named {
  boolean immutable();
  int id();
  boolean many();
  boolean mandatory();
  List<GenericParameter> parameters();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = 2122316949;
    int ID = 1492330856;
    int MANY = -389004436;
    int MANDATORY = 132418796;
    int PARAMETERS = -435928777;
  }
}
