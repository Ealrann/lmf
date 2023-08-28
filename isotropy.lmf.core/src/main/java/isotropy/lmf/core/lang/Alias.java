package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AliasBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;

public interface Alias extends Named
{
	List<String> words();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<String, List<String>> words = new RawFeature<>(true,
																  false,
																  () -> LMCoreDefinition.Features.ALIAS.words);
	}

	static Alias.Builder builder() {return new AliasBuilder();}
	interface Builder extends LMObject.Builder<Alias>
	{
		Builder name(String name);

		Builder addWord(String word);
	}
}
