package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AliasBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;

import java.util.List;

public interface Alias extends Named
{
	List<String> words();

	static Alias.Builder builder() {return new AliasBuilder();}
	interface Builder extends LMObject.Builder<Alias>
	{
		Builder name(String name);

		Builder addWord(String word);
	}
}
