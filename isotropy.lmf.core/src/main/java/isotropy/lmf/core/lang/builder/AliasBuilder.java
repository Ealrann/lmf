package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.AliasImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.IFeaturedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class AliasBuilder implements Alias.Builder
{
	private static final FeatureInserter<AliasBuilder> FEATURE_INSERTER = FeatureInserter.<AliasBuilder>Builder()

			.add(Alias.Features.name, AliasBuilder::name)
			.add(Alias.Features.words, AliasBuilder::addWord)
			.build();

	private String name = null;
	private List<String> words = new ArrayList<>();

	@Override
	public AliasImpl build()
	{
		return new AliasImpl(name, words);
	}

	@Override
	public <Type> void push(final Feature<Type, ?> feature, final Type value)
	{
		FEATURE_INSERTER.push(this, feature, value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
	}

	@Override
	public AliasBuilder name(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public AliasBuilder addWord(final String word)
	{
		this.words.add(word);
		return this;
	}
}
