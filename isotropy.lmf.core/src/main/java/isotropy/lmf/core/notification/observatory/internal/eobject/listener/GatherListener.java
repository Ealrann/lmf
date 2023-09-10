package isotropy.lmf.core.notification.observatory.internal.eobject.listener;

import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.function.Consumer;

public record GatherListener<T extends ILilyEObject>(Consumer<T>discoverObject, Consumer<T>removedObject)
{
}
