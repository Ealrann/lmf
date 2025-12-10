package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.MetaModel.Builder;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.lang.impl.MetaModelImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class MetaModelBuilder implements Builder {
  private String name;
  private String domain;
  private final List<String> imports = new ArrayList<>();
  private final List<String> metamodels = new ArrayList<>();
  private final List<Supplier<Group<?>>> groups = new ArrayList<>();
  private final List<Supplier<Enum<?>>> enums = new ArrayList<>();
  private final List<Supplier<Unit<?>>> units = new ArrayList<>();
  private final List<Supplier<Alias>> aliases = new ArrayList<>();
  private final List<Supplier<JavaWrapper<?>>> javaWrappers = new ArrayList<>();
  private IModelPackage lmPackage;
  private boolean genNamePackage = true;
  private String extraPackage;

  public MetaModelBuilder() {
  }

  @Override
  public MetaModelBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public MetaModelBuilder domain(String domain) {
    this.domain = domain;
    return this;
  }

  @Override
  public MetaModelBuilder addImport(String _import) {
    this.imports.add(_import);
    return this;
  }

  @Override
  public MetaModelBuilder addImports(final List<String> imports) {
    this.imports.addAll(imports);
    return this;
  }

  @Override
  public MetaModelBuilder addMetamodel(String metamodel) {
    this.metamodels.add(metamodel);
    return this;
  }

  @Override
  public MetaModelBuilder addMetamodels(final List<String> metamodels) {
    this.metamodels.addAll(metamodels);
    return this;
  }

  @Override
  public MetaModelBuilder addGroup(Supplier<Group<?>> group) {
    this.groups.add(group);
    return this;
  }

  @Override
  public MetaModelBuilder addGroups(final List<Group<?>> groups) {
    groups.stream().map(value -> (Supplier<Group<?>>) () -> value).forEach(this.groups::add);
    return this;
  }

  @Override
  public MetaModelBuilder addEnum(Supplier<Enum<?>> _enum) {
    this.enums.add(_enum);
    return this;
  }

  @Override
  public MetaModelBuilder addEnums(final List<Enum<?>> enums) {
    enums.stream().map(value -> (Supplier<Enum<?>>) () -> value).forEach(this.enums::add);
    return this;
  }

  @Override
  public MetaModelBuilder addUnit(Supplier<Unit<?>> unit) {
    this.units.add(unit);
    return this;
  }

  @Override
  public MetaModelBuilder addUnits(final List<Unit<?>> units) {
    units.stream().map(value -> (Supplier<Unit<?>>) () -> value).forEach(this.units::add);
    return this;
  }

  @Override
  public MetaModelBuilder addAliase(Supplier<Alias> aliase) {
    this.aliases.add(aliase);
    return this;
  }

  @Override
  public MetaModelBuilder addAliases(final List<Alias> aliases) {
    aliases.stream().map(value -> (Supplier<Alias>) () -> value).forEach(this.aliases::add);
    return this;
  }

  @Override
  public MetaModelBuilder addJavaWrapper(Supplier<JavaWrapper<?>> javaWrapper) {
    this.javaWrappers.add(javaWrapper);
    return this;
  }

  @Override
  public MetaModelBuilder addJavaWrappers(final List<JavaWrapper<?>> javaWrappers) {
    javaWrappers.stream().map(value -> (Supplier<JavaWrapper<?>>) () -> value).forEach(this.javaWrappers::add);
    return this;
  }

  @Override
  public MetaModelBuilder lmPackage(IModelPackage lmPackage) {
    this.lmPackage = lmPackage;
    return this;
  }

  @Override
  public MetaModelBuilder genNamePackage(boolean genNamePackage) {
    this.genNamePackage = genNamePackage;
    return this;
  }

  @Override
  public MetaModelBuilder extraPackage(String extraPackage) {
    this.extraPackage = extraPackage;
    return this;
  }

  @Override
  public MetaModel build() {
    final var builtGroups = BuildUtils.collectSuppliers(groups);
    final var builtEnums = BuildUtils.collectSuppliers(enums);
    final var builtUnits = BuildUtils.collectSuppliers(units);
    final var builtAliases = BuildUtils.collectSuppliers(aliases);
    final var builtJavaWrappers = BuildUtils.collectSuppliers(javaWrappers);
    final var built = new MetaModelImpl(name, domain, imports, metamodels, builtGroups, builtEnums, builtUnits, builtAliases, builtJavaWrappers, lmPackage, genNamePackage, extraPackage);
    return built;
  }

  @Override
  public <AttributeType> void push(final Attribute<AttributeType, ?> attribute,
      final AttributeType value) {
    Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
      final Supplier<RelationType> supplier) {
    Inserters.RELATION_INSERTER.push(this, relation.id(), supplier);
  }

  private static int attributeIndex(final int featureId) {
    return switch (featureId) {
      case MetaModel.FeatureIDs.NAME -> 0;
      case MetaModel.FeatureIDs.DOMAIN -> 1;
      case MetaModel.FeatureIDs.IMPORTS -> 2;
      case MetaModel.FeatureIDs.METAMODELS -> 3;
      case MetaModel.FeatureIDs.LM_PACKAGE -> 4;
      case MetaModel.FeatureIDs.GEN_NAME_PACKAGE -> 5;
      case MetaModel.FeatureIDs.EXTRA_PACKAGE -> 6;
      default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
    };
  }

  private static int relationIndex(final int featureId) {
    return switch (featureId) {
      case MetaModel.FeatureIDs.GROUPS -> 0;
      case MetaModel.FeatureIDs.ENUMS -> 1;
      case MetaModel.FeatureIDs.UNITS -> 2;
      case MetaModel.FeatureIDs.ALIASES -> 3;
      case MetaModel.FeatureIDs.JAVA_WRAPPERS -> 4;
      default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
    };
  }

  private static final class Inserters {
    private static final FeatureInserter<MetaModelBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<MetaModelBuilder>(7, MetaModelBuilder::attributeIndex).add(MetaModel.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(MetaModel.FeatureIDs.DOMAIN, (builder, value) -> builder.domain((String) value)).add(MetaModel.FeatureIDs.IMPORTS, (builder, value) -> builder.addImport((String) value)).add(MetaModel.FeatureIDs.METAMODELS, (builder, value) -> builder.addMetamodel((String) value)).add(MetaModel.FeatureIDs.LM_PACKAGE, (builder, value) -> builder.lmPackage((IModelPackage) value)).add(MetaModel.FeatureIDs.GEN_NAME_PACKAGE, (builder, value) -> builder.genNamePackage((boolean) value)).add(MetaModel.FeatureIDs.EXTRA_PACKAGE, (builder, value) -> builder.extraPackage((String) value)).build();
    private static final RelationLazyInserter<MetaModelBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<MetaModelBuilder>(5, MetaModelBuilder::relationIndex).add(MetaModel.FeatureIDs.GROUPS, (builder, value) -> builder.addGroup((Supplier<Group<?>>) value)).add(MetaModel.FeatureIDs.ENUMS, (builder, value) -> builder.addEnum((Supplier<Enum<?>>) value)).add(MetaModel.FeatureIDs.UNITS, (builder, value) -> builder.addUnit((Supplier<Unit<?>>) value)).add(MetaModel.FeatureIDs.ALIASES, (builder, value) -> builder.addAliase((Supplier<Alias>) value)).add(MetaModel.FeatureIDs.JAVA_WRAPPERS, (builder, value) -> builder.addJavaWrapper((Supplier<JavaWrapper<?>>) value)).build();
  }
}
