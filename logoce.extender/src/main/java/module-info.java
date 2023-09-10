import org.logoce.extender.api.IAdapterDescriptorRegistry;
import org.logoce.extender.api.IAdapterExtension;
import org.logoce.extender.api.IAdapterProvider;
import org.logoce.extender.ext.IAdaptableNameMatcher;
import org.logoce.extender.ext.IAdapterHandleFactory;
import org.logoce.extender.impl.AdapterDescriptorRegistry;

module logoce.extender {
	exports org.logoce.extender.api;
	exports org.logoce.extender.api.parameter;
	exports org.logoce.extender.api.reflect;

	exports org.logoce.extender.ext;

	uses IAdapterDescriptorRegistry;
	uses IAdapterHandleFactory;
	uses IAdapterProvider;
	uses IAdaptableNameMatcher;
	uses IAdapterExtension.Descriptor;

	provides IAdapterDescriptorRegistry with AdapterDescriptorRegistry;
}
