package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.EnumAttributeBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;

public interface EnumAttribute extends Named {
  static Builder builder() {
    return new EnumAttributeBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  Unit<?> unit();
  String defaultValue();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int UNIT = 170240962;
    int DEFAULT_VALUE = 541474798;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Relation<Unit<?>, Unit<?>, Listener<Unit<?>>, Features<?>> UNIT = new RelationBuilder<Unit<?>, Unit<?>, Listener<Unit<?>>, Features<?>>().name("unit").immutable(true).mandatory(true).id(EnumAttribute.FeatureIDs.UNIT).concept(() -> LMCoreModelDefinition.Groups.UNIT).build();
    Attribute<String, String, Listener<String>, Features<?>> DEFAULT_VALUE = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("defaultValue").immutable(true).id(EnumAttribute.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, UNIT, DEFAULT_VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<EnumAttribute> {
    Builder name(String name);
    Builder unit(Supplier<Unit<?>> unit);
    Builder defaultValue(String defaultValue);
  }
}
