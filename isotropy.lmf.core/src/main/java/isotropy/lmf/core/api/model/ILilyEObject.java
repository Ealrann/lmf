package isotropy.lmf.core.api.model;

import isotropy.lmf.core.lang.LMObject;
import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapter;

import java.util.stream.Stream;

public interface ILilyEObject extends IEMFNotifier, IAdaptable
{
	Stream<LMObject> streamChildren();
	Stream<LMObject> streamTree();

	@Override
	<T extends IAdapter> T adapt(Class<T> type);
	@Override
	<T extends IAdapter> T adapt(Class<T> type, String identifier);
	<T extends IAdapter> T adaptNotNull(Class<T> type);
	<T extends IAdapter> T adaptNotNull(Class<T> type, String identifier);

	<T extends IAdapter> T adaptGeneric(Class<? extends IAdapter> type);
	<T extends IAdapter> T adaptNotNullGeneric(Class<? extends IAdapter> type);
}
