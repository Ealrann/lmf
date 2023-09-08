package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Alias;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;

import java.util.List;

public final class AliasImpl extends FeaturedObject implements Alias
{
	private static final FeatureGetter<Alias> GET_MAP = new FeatureGetter.Builder<Alias>().add(Features.name,
																							   Alias::name)
																						  .add(Features.words,
																							   Alias::words)
																						  .build();

	private static final FeatureSetter<Alias> SET_MAP = new FeatureSetter.Builder<Alias>().build();

	private final String name;

	private final List<String> words;

	public AliasImpl(final String name, final List<String> words)
	{
		this.name = name;
		this.words = List.copyOf(words);
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
	public Group<Alias> lmGroup()
	{
		return LMCoreDefinition.Groups.ALIAS;
	}

	@Override
	protected FeatureSetter<Alias> setterMap()
	{
		return SET_MAP;
	}

	@Override
	protected FeatureGetter<Alias> getterMap()
	{
		return GET_MAP;
	}
}
