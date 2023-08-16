package isotropy.lmf.core.resource.transform.node;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.IModelPackage;

public record ModelGroup<T extends LMObject>(IModelPackage modelPackage, Group<T> group)
{
	public String name()
	{
		return group.name();
	}

	public IFeaturedObject.Builder<T> builder()
	{
		return modelPackage.builder(group);
	}
}
