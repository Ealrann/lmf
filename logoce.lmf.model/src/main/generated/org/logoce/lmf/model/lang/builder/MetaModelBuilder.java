package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
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
  private static final FeatureInserter<MetaModelBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<MetaModelBuilder>().add(MetaModel.Features.name, MetaModelBuilder::name).add(MetaModel.Features.domain, MetaModelBuilder::domain).add(MetaModel.Features.imports, MetaModelBuilder::addImport).add(MetaModel.Features.lmPackage, MetaModelBuilder::lmPackage).add(MetaModel.Features.genNamePackage, MetaModelBuilder::genNamePackage).add(MetaModel.Features.extraPackage, MetaModelBuilder::extraPackage).build();
  private static final RelationLazyInserter<MetaModelBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<MetaModelBuilder>().add(MetaModel.Features.groups, MetaModelBuilder::addGroup).add(MetaModel.Features.enums, MetaModelBuilder::addEnum).add(MetaModel.Features.units, MetaModelBuilder::addUnit).add(MetaModel.Features.aliases, MetaModelBuilder::addAliase).add(MetaModel.Features.javaWrappers, MetaModelBuilder::addJavaWrapper).build();
  private String name;
  private String domain;
  private final List<String> imports = new ArrayList<>();
  private final List<Supplier<Group<?>>> groups = new ArrayList<>();
  private final List<Supplier<Enum<?>>> enums = new ArrayList<>();
  private final List<Supplier<Unit<?>>> units = new ArrayList<>();
  private final List<Supplier<Alias>> aliases = new ArrayList<>();
  private final List<Supplier<JavaWrapper<?>>> javaWrappers = new ArrayList<>();
  private IModelPackage lmPackage;
  private boolean genNamePackage;
  private String extraPackage;

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
    final var built = new MetaModelImpl(name, domain, imports, builtGroups, builtEnums, builtUnits, builtAliases, builtJavaWrappers, lmPackage, genNamePackage, extraPackage);
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
