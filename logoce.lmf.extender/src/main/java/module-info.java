import org.logoce.lmf.extender.api.IAdapterDescriptorRegistry;
import org.logoce.lmf.extender.api.IAdapterExtension;
import org.logoce.lmf.extender.api.IAdapterProvider;
import org.logoce.lmf.extender.ext.IAdaptableNameMatcher;
import org.logoce.lmf.extender.ext.IAdapterHandleFactory;
import org.logoce.lmf.extender.impl.AdapterDescriptorRegistry;

module logoce.lmf.extender {
	exports org.logoce.lmf.extender.api;
	exports org.logoce.lmf.extender.api.parameter;
	exports org.logoce.lmf.extender.api.reflect;

	exports org.logoce.lmf.extender.ext;

	uses IAdapterDescriptorRegistry;
	uses IAdapterHandleFactory;
	uses IAdapterProvider;
	uses IAdaptableNameMatcher;
	uses IAdapterExtension.Descriptor;

	provides IAdapterDescriptorRegistry with AdapterDescriptorRegistry;
}
