package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;

import java.util.List;

public final class AliasImpl implements Alias
{
	public static final FeatureGetter<Alias> FEATURE_GETTER = new FeatureGetter.Builder<Alias>()

			.add(Features.name, Named::name)
			.add(Features.words, Alias::words)
			.build();

	private final String name;
	private final List<String> words;

	private LMObject container;

	public AliasImpl(final String name, final List<String> words)
	{
		this.name = name;
		this.words = words;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<String> words()
	{
		return words;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return FEATURE_GETTER.get(this, feature);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Alias.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return Alias.GROUP;
	}

	@Override
	public LMObject lContainer()
	{
		return container;
	}

	@Override
	public void lContainer(final LMObject container)
	{
		this.container = container;
	}
}
