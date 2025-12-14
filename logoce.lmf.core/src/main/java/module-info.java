import org.logoce.lmf.extender.api.IAdapterProvider;
import org.logoce.lmf.core.Extenders;

module logoce.lmf.core
{
	requires transitive logoce.lmf.adapter;
	requires transitive logoce.lmf.extender;
	requires transitive logoce.lmf.notification;

	exports org.logoce.lmf.core.lang;
	exports org.logoce.lmf.core.lang.impl;
	exports org.logoce.lmf.core.lang.builder;
	exports org.logoce.lmf.core.lexer;
	exports org.logoce.lmf.core.notification.list;
	exports org.logoce.lmf.core.resource.interpretation;
	exports org.logoce.lmf.core.resource.parsing;
	exports org.logoce.lmf.core.util;
	exports org.logoce.lmf.core.util.tree;
	exports org.logoce.lmf.core.loader;
	exports org.logoce.lmf.core.loader.diagnostic;
	exports org.logoce.lmf.core.loader.linking;
	exports org.logoce.lmf.core.loader.linking.feature;
	exports org.logoce.lmf.core.loader.linking.feature.reference;
	exports org.logoce.lmf.core.loader.linking.tree;
	exports org.logoce.lmf.core.loader.linking.linker;
	exports org.logoce.lmf.core.loader.model;
	exports org.logoce.lmf.core.api.notification;
	exports org.logoce.lmf.core.notification.impl;
	exports org.logoce.lmf.core.notification.listener;
	exports org.logoce.lmf.core.notification.observatory;
	exports org.logoce.lmf.core.notification.util;
	exports org.logoce.lmf.core.api.feature;
	exports org.logoce.lmf.core.api.model;

	exports org.logoce.lmf.core.feature;

	provides IAdapterProvider with Extenders;
}
