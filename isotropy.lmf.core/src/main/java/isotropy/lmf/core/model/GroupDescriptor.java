package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;

import java.util.function.Supplier;

public record GroupDescriptor<T extends LMObject>(Group<T> group, Supplier<IFeaturedObject.Builder<? extends T>> builder)
{
}
