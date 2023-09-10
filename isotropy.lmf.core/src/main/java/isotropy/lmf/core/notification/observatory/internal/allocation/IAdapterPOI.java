package isotropy.lmf.core.notification.observatory.internal.allocation;

import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterHandle;

public interface IAdapterPOI<T extends IAdapter>
{
	void listen(IAdapterHandle<T> handle);
	void sulk(IAdapterHandle<T> handle);
}
