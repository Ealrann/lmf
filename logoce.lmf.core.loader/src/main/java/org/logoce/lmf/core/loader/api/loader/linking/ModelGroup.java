package org.logoce.lmf.core.loader.api.loader.linking;

import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;

public record ModelGroup<T extends LMObject>(IModelPackage modelPackage, Group<T> group)
{
	public String name()
	{
		return group.name();
	}

	public IFeaturedObject.Builder<T> builder()
	{
		return modelPackage.builder(group).orElseThrow();
	}
}
