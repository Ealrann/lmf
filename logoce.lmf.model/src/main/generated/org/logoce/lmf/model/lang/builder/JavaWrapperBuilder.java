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
  private static final FeatureInserter<JavaWrapperBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<JavaWrapperBuilder<?>>().add(JavaWrapper.RFeatures.name, JavaWrapperBuilder::name).add(JavaWrapper.RFeatures.qualifiedClassName, JavaWrapperBuilder::qualifiedClassName).build();
  private static final RelationLazyInserter<JavaWrapperBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<JavaWrapperBuilder<?>>().add(JavaWrapper.RFeatures.serializer, JavaWrapperBuilder::serializer).build();
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
  public <AttributeType> void push(final Attribute<AttributeType, ?> attribute,
      final AttributeType value) {
    ATTRIBUTE_INSERTER.push(this, attribute.rawFeature(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
      final Supplier<RelationType> supplier) {
    RELATION_INSERTER.push(this, relation.rawFeature(), supplier);
  }
}
