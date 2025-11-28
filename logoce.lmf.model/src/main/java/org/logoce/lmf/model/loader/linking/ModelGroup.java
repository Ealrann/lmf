package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;

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

