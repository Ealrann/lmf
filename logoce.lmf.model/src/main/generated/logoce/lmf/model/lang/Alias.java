package logoce.lmf.model.lang;

import java.lang.String;
import java.util.List;
import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.model.IFeaturedObject;
import logoce.lmf.model.lang.builder.AliasBuilder;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  List<String> words();

  interface Features extends Named.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, List<String>> words = new RawFeature<>(true,false,() -> LMCoreDefinition.Features.ALIAS.WORDS);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);

    Builder addWord(String word);
  }
}
