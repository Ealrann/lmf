package logoce.lmf.model.resource.transform.node;

import logoce.lmf.model.api.model.IFeaturedObject;
import logoce.lmf.model.api.model.IModelPackage;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.LMObject;

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
