package logoce.lmf.model.notification.observatory.internal.allocation;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterHandle;

public interface IAdapterPOI<T extends IAdapter>
{
	void listen(IAdapterHandle<T> handle);
	void sulk(IAdapterHandle<T> handle);
}
