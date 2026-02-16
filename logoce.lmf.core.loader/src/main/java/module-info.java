import org.logoce.lmf.core.api.service.ILmLoader;
import org.logoce.lmf.core.loader.service.LMLoader;

module logoce.lmf.core.loader
{
	requires transitive logoce.lmf.core.api;

	exports org.logoce.lmf.core.loader.api.lexer;
	exports org.logoce.lmf.core.loader.api.text.syntax;
	exports org.logoce.lmf.core.loader.api.text.parsing;
	exports org.logoce.lmf.core.loader.api.loader;
	exports org.logoce.lmf.core.loader.api.loader.diagnostic;
	exports org.logoce.lmf.core.loader.api.loader.linking;
	exports org.logoce.lmf.core.loader.api.loader.linking.tree;
	exports org.logoce.lmf.core.loader.api.loader.model;
	exports org.logoce.lmf.core.loader.api.loader.parsing;
	exports org.logoce.lmf.core.loader.api.loader.util;
	exports org.logoce.lmf.core.loader.api.tooling;
	exports org.logoce.lmf.core.loader.api.tooling.state;
	exports org.logoce.lmf.core.loader.api.tooling.validation;
	exports org.logoce.lmf.core.loader.api.tooling.workspace;
	exports org.logoce.lmf.core.util.tree;

	uses ILmLoader;
	provides ILmLoader with LMLoader;
}
