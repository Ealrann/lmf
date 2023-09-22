import org.logoce.lmf.adapter.impl.AdapterHandleFactory;
import org.logoce.lmf.extender.ext.IAdapterHandleFactory;

module logoce.lmf.adapter {
	requires transitive logoce.lmf.extender;

	exports org.logoce.lmf.adapter.api;

	provides IAdapterHandleFactory with AdapterHandleFactory;
}
