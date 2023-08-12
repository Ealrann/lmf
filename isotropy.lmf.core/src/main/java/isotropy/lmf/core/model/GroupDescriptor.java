package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;

import java.util.function.Supplier;

public final class GroupDescriptor<T extends LMObject>
{
	public final Group<T> group;
	public final Supplier<IFeaturedObject.Builder<? extends T>> builder;

	public GroupDescriptor(final Group<T> group, final Supplier<IFeaturedObject.Builder<? extends T>> builder)
	{
		this.group = group;
		this.builder = builder;
	}

	public Group<T> group()
	{
		return group;
	}

	public Supplier<IFeaturedObject.Builder<? extends T>> builder()
	{
		return builder;
	}
}
