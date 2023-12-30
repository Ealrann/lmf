import org.logoce.lmf.generator.Extenders;
import org.logoce.lmf.extender.api.IAdapterProvider;

module logoce.lmf.generator {
	requires transitive logoce.lmf.model;
	requires com.squareup.javapoet;
	requires java.compiler;

	exports org.logoce.lmf.generator;

	opens org.logoce.lmf.generator.adapter;

	provides IAdapterProvider with Extenders;
}
