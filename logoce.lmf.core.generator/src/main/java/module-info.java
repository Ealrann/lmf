import org.logoce.lmf.core.api.extender.IAdapterProvider;
import org.logoce.lmf.generator.Extenders;

module logoce.lmf.core.generator {
	requires transitive logoce.lmf.core.api;
	requires logoce.lmf.core.loader;
	requires com.squareup.javapoet;
	requires java.compiler;

	exports org.logoce.lmf.generator;

	opens org.logoce.lmf.generator.adapter;

	provides IAdapterProvider with Extenders;
}
