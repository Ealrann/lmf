package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AliasBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;

import java.util.List;

public interface Alias extends Named
{
	List<String> words();

	Group<Alias> GROUP = LMCorePackage.ALIAS_GROUP;

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Attribute<String, List<String>> words = new AttributeImpl<>("words",
																	true,
																	true,
																	false,
																	LMCorePackage.STRING_UNIT);

		List<Feature<?, ?>> All = List.of(name, words);
	}

	static Alias.Builder builder() {return new AliasBuilder();}
	interface Builder extends LMObject.Builder<Alias>
	{
		Builder name(String name);

		Builder addWord(String word);
	}
}
