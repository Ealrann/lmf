import org.logoce.lmf.extender.api.IAdapterProvider;
import org.logoce.lmf.core.Extenders;

module logoce.lmf.core.api
{
	requires transitive logoce.lmf.adapter;
	requires transitive logoce.lmf.extender;
	requires transitive logoce.lmf.notification;

	exports org.logoce.lmf.core.lang;
	exports org.logoce.lmf.core.lang.builder;
	exports org.logoce.lmf.core.lexer;
	exports org.logoce.lmf.core.api.notification.list;
	exports org.logoce.lmf.core.util;
	exports org.logoce.lmf.core.util.tree;
	exports org.logoce.lmf.core.api.notification;
	exports org.logoce.lmf.core.api.notification.listener;
	exports org.logoce.lmf.core.api.notification.util;
	exports org.logoce.lmf.core.api.feature;
	exports org.logoce.lmf.core.api.model;
	exports org.logoce.lmf.core.feature;
	exports org.logoce.lmf.core.api.text.syntax;
	exports org.logoce.lmf.core.api.text.parsing;
	exports org.logoce.lmf.core.api.notification.observatory;
	exports org.logoce.lmf.core.api.util;
	exports org.logoce.lmf.core.api.loader;
	exports org.logoce.lmf.core.api.loader.diagnostic;
	exports org.logoce.lmf.core.api.loader.linking;
	exports org.logoce.lmf.core.api.loader.linking.tree;
	exports org.logoce.lmf.core.api.loader.model;
	exports org.logoce.lmf.core.api.loader.parsing;

	provides IAdapterProvider with Extenders;
}
