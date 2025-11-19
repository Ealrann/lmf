package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.lang.LMObject;

import java.util.function.Supplier;

public record BuilderSupplier<T extends LMObject> (Supplier<IFeaturedObject.Builder<T>> supplier)
{
	public IFeaturedObject.Builder<T> newBuilder()
	{
		return supplier.get();
	}
}
