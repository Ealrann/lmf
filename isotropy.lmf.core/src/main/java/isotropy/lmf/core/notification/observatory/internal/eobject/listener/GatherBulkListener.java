package isotropy.lmf.core.notification.observatory.internal.eobject.listener;

import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.List;
import java.util.function.Consumer;

public record GatherBulkListener<T extends ILilyEObject>(Consumer<List<T>>discoverObjects,
														 Consumer<List<T>>removedObjects)
{
}