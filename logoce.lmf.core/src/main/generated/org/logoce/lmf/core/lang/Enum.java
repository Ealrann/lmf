package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.EnumBuilder;

public interface Enum<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new EnumBuilder<>();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  List<String> literals();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int LITERALS = -114065442;
  }

  interface Features<T extends Features<T>> extends Datatype.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<String, List<String>, Listener<List<String>>, Features<?>> LITERALS = new AttributeBuilder<String, List<String>, Listener<List<String>>, Features<?>>().name("literals").immutable(true).many(true).id(Enum.FeatureIDs.LITERALS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, LITERALS);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Enum<T>> {
    Builder<T> name(String name);
    Builder<T> addLiteral(String literal);
    Builder<T> addLiterals(List<String> literals);
  }
}
