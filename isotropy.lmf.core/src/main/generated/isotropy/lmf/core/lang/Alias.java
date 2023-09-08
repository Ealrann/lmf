package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AliasBuilder;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.RawFeature;
import java.lang.String;
import java.util.List;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  List<String> words();

  interface Features {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, List<String>> words = new RawFeature<>(true,false,() -> LMCoreDefinition.Features.ALIAS.WORDS);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);

    Builder addWord(String word);
  }
}
