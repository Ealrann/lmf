import logoce.lmf.generator.Extenders;
import org.logoce.lmf.extender.api.IAdapterProvider;

module logoce.lmf.generator {
	requires logoce.lmf.model;
	requires com.squareup.javapoet;
	requires java.compiler;

	exports logoce.lmf.generator;

	opens logoce.lmf.generator.adapter;

	provides IAdapterProvider with Extenders;
}
