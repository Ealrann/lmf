import isotropy.lmf.generator.Extenders;
import org.logoce.extender.api.IAdapterProvider;

module isotropy.lmf.generator {
	requires isotropy.lmf.core;
	requires com.squareup.javapoet;
	requires java.compiler;

	exports isotropy.lmf.generator;

	opens isotropy.lmf.generator.adapter;

	provides IAdapterProvider with Extenders;
}
