package org.logoce.lmf.model.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.JavaWrapper.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Serializer;
import org.logoce.lmf.model.lang.impl.JavaWrapperImpl;

public final class JavaWrapperBuilder<T> implements Builder<T> {
  private String name;
  private String qualifiedClassName;
  private Supplier<Serializer> serializer = () -> null;

  public JavaWrapperBuilder() {
  }

  @Override
  public JavaWrapperBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public JavaWrapperBuilder<T> qualifiedClassName(String qualifiedClassName) {
    this.qualifiedClassName = qualifiedClassName;
    return this;
  }

  @Override
  public JavaWrapperBuilder<T> serializer(Supplier<Serializer> serializer) {
    this.serializer = serializer;
    return this;
  }

  @Override
  public JavaWrapper<T> build() {
    final var built = new JavaWrapperImpl<T>(name, qualifiedClassName, serializer.get());
    return built;
  }

  @Override
  public <AttributeType> void push(final Attribute<?, ?, ?, ?> attribute,
      final AttributeType value) {
    Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?, ?, ?> relation,
      final Supplier<RelationType> supplier) {
    Inserters.RELATION_INSERTER.push(this, relation.id(), supplier);
  }

  private static final class Inserters {
    private static final FeatureInserter<JavaWrapperBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<JavaWrapperBuilder>(2, Inserters::attributeIndex).add(JavaWrapper.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME, (builder, value) -> builder.qualifiedClassName((String) value)).build();
    private static final RelationLazyInserter<JavaWrapperBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<JavaWrapperBuilder>(1, Inserters::relationIndex).add(JavaWrapper.FeatureIDs.SERIALIZER, (builder, value) -> builder.serializer((Supplier<Serializer>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case JavaWrapper.FeatureIDs.NAME -> 0;
        case JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case JavaWrapper.FeatureIDs.SERIALIZER -> 0;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
