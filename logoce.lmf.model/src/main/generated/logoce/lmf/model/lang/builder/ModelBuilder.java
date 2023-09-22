package logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import logoce.lmf.model.api.model.IModelPackage;
import logoce.lmf.model.feature.FeatureInserter;
import logoce.lmf.model.feature.RelationLazyInserter;
import logoce.lmf.model.lang.Alias;
import logoce.lmf.model.lang.Attribute;
import logoce.lmf.model.lang.Enum;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.JavaWrapper;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.lang.Model.Builder;
import logoce.lmf.model.lang.Relation;
import logoce.lmf.model.lang.Unit;
import logoce.lmf.model.lang.impl.ModelImpl;
import logoce.lmf.model.util.BuildUtils;

public final class ModelBuilder implements Builder {
  private static final FeatureInserter<ModelBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<ModelBuilder>().add(Model.Features.name, ModelBuilder::name).add(Model.Features.domain, ModelBuilder::domain).add(Model.Features.lPackage, ModelBuilder::lPackage).build();

  private static final RelationLazyInserter<ModelBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<ModelBuilder>().add(Model.Features.groups, ModelBuilder::addGroup).add(Model.Features.enums, ModelBuilder::addEnum).add(Model.Features.units, ModelBuilder::addUnit).add(Model.Features.aliases, ModelBuilder::addAliase).add(Model.Features.javaWrappers, ModelBuilder::addJavaWrapper).build();

  private String name;

  private String domain;

  private final List<Supplier<Group<?>>> groups = new ArrayList<>();

  private final List<Supplier<Enum<?>>> enums = new ArrayList<>();

  private final List<Supplier<Unit<?>>> units = new ArrayList<>();

  private final List<Supplier<Alias>> aliases = new ArrayList<>();

  private final List<Supplier<JavaWrapper<?>>> javaWrappers = new ArrayList<>();

  private IModelPackage lPackage;

  @Override
  public ModelBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public ModelBuilder domain(String domain) {
    this.domain = domain;
    return this;
  }

  @Override
  public ModelBuilder addGroup(Supplier<Group<?>> group) {
    this.groups.add(group);
    return this;
  }

  @Override
  public ModelBuilder addEnum(Supplier<Enum<?>> _enum) {
    this.enums.add(_enum);
    return this;
  }

  @Override
  public ModelBuilder addUnit(Supplier<Unit<?>> unit) {
    this.units.add(unit);
    return this;
  }

  @Override
  public ModelBuilder addAliase(Supplier<Alias> aliase) {
    this.aliases.add(aliase);
    return this;
  }

  @Override
  public ModelBuilder addJavaWrapper(Supplier<JavaWrapper<?>> javaWrapper) {
    this.javaWrappers.add(javaWrapper);
    return this;
  }

  @Override
  public ModelBuilder lPackage(IModelPackage lPackage) {
    this.lPackage = lPackage;
    return this;
  }

  @Override
  public Model build() {
    final var builtGroups = BuildUtils.collectSuppliers(groups);
    final var builtEnums = BuildUtils.collectSuppliers(enums);
    final var builtUnits = BuildUtils.collectSuppliers(units);
    final var builtAliases = BuildUtils.collectSuppliers(aliases);
    final var builtJavaWrappers = BuildUtils.collectSuppliers(javaWrappers);
    return new ModelImpl(name, domain, builtGroups, builtEnums, builtUnits, builtAliases, builtJavaWrappers, lPackage);
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
