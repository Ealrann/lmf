package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.lang.LMObject;

import java.util.function.Supplier;

public record BuilderSupplier<T extends LMObject> (Supplier<? extends IFeaturedObject.Builder<? extends T>> supplier)
{
	@SuppressWarnings("unchecked")
	public IFeaturedObject.Builder<T> newBuilder()
	{
		return (IFeaturedObject.Builder<T>) supplier.get();
	}
}
