import org.logoce.lmf.core.adapter.AdapterHandleFactory;
import org.logoce.lmf.core.api.extender.IAdapterDescriptorRegistry;
import org.logoce.lmf.core.api.extender.IAdapterExtension;
import org.logoce.lmf.core.api.extender.IAdapterProvider;
import org.logoce.lmf.core.Extenders;
import org.logoce.lmf.core.api.service.ILmLoader;
import org.logoce.lmf.core.api.extender.ext.IAdaptableNameMatcher;
import org.logoce.lmf.core.api.extender.ext.IAdapterHandleFactory;
import org.logoce.lmf.core.api.extender.impl.AdapterDescriptorRegistry;

module logoce.lmf.core.api
{
	exports org.logoce.lmf.core.lang;
	exports org.logoce.lmf.core.lang.builder;
	exports org.logoce.lmf.core.api.adapter;
	exports org.logoce.lmf.core.api.extender;
	exports org.logoce.lmf.core.api.extender.parameter;
	exports org.logoce.lmf.core.api.extender.reflect;
	exports org.logoce.lmf.core.api.notification.list;
	exports org.logoce.lmf.core.api.notification;
	exports org.logoce.lmf.core.api.notification.listener;
	exports org.logoce.lmf.core.api.notification.util;
	exports org.logoce.lmf.core.api.feature;
	exports org.logoce.lmf.core.api.model;
	exports org.logoce.lmf.core.api.notification.observatory;
	exports org.logoce.lmf.core.api.util;
	exports org.logoce.lmf.core.api.extender.ext;
	exports org.logoce.lmf.core.api.service;

	exports org.logoce.lmf.core.util to logoce.lmf.core.loader, logoce.lmf.core.generator;
	exports org.logoce.lmf.core.feature to logoce.lmf.core.loader, logoce.lmf.core.generator;

	uses ILmLoader;
	uses IAdapterDescriptorRegistry;
	uses IAdapterHandleFactory;
	uses IAdapterProvider;
	uses IAdaptableNameMatcher;
	uses IAdapterExtension.Descriptor;

	provides IAdapterProvider with Extenders;
	provides IAdapterHandleFactory with AdapterHandleFactory;
	provides IAdapterDescriptorRegistry with AdapterDescriptorRegistry;
}
