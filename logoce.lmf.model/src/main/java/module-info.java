import org.logoce.lmf.extender.api.IAdapterProvider;
import org.logoce.lmf.model.Extenders;

module logoce.lmf.model
{
	requires transitive logoce.lmf.adapter;
	requires transitive logoce.lmf.extender;
	requires transitive logoce.lmf.notification;

	exports org.logoce.lmf.model.lang;
	exports org.logoce.lmf.model.lang.impl;
	exports org.logoce.lmf.model.lexer;
	exports org.logoce.lmf.model.resource.parsing;
	exports org.logoce.lmf.model.resource;
	exports org.logoce.lmf.model.util;
	exports org.logoce.lmf.model.util.tree;
	exports org.logoce.lmf.model.api.notification;
	exports org.logoce.lmf.model.api.feature;
	exports org.logoce.lmf.model.api.model;

	exports org.logoce.lmf.model.feature to logoce.lmf.generator;
	exports org.logoce.lmf.model.resource.interpretation;

	provides IAdapterProvider with Extenders;
}
