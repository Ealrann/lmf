package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Named extends LMObject {
  String name();

  interface RFeatures<T extends RFeatures<T>> extends LMObject.RFeatures<T> {
    RawFeature<String, String> name = new RawFeature<>(false,false,() -> Named.Features.NAME);
  }

  interface FeatureIDs {
    int NAME = 1328200565;
  }

  interface Features {
    Attribute<String, String> NAME = new AttributeBuilder<String, String>().name("name").immutable(true).mandatory(true).rawFeature(Named.RFeatures.name).id(Named.FeatureIDs.NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME);
  }
}
