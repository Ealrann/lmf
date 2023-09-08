package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Alias;
import isotropy.lmf.core.lang.Alias.Builder;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.impl.AliasImpl;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class AliasBuilder implements Builder {
  private static final FeatureInserter<AliasBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<AliasBuilder>().add(Alias.Features.name, AliasBuilder::name).add(Alias.Features.words, AliasBuilder::addWord).build();

  private static final RelationLazyInserter<AliasBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<AliasBuilder>().build();

  private String name;

  private final List<String> words = new ArrayList<>();

  @Override
  public AliasBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AliasBuilder addWord(String word) {
    this.words.add(word);
    return this;
  }

  @Override
  public Alias build() {
    return new AliasImpl(name, words);
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
