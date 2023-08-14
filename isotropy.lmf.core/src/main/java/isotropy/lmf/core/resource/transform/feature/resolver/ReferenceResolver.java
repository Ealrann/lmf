package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.util.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.Optional;

public final class ReferenceResolver<T extends LMObject> extends AbstractResolver<T, Relation<T, ?>> implements
																									 IWordResolver<T>
{
	public ReferenceResolver(final Relation<T, ?> relation)
	{
		super(relation);
		assert !relation.contains();
	}

	@Override
	public Optional<IFeatureResolution> resolve(Tree<BuilderNode<?>> tree, String value)
	{
		if (value.startsWith("/") || value.startsWith("./") || value.startsWith("../"))
		{

		}
		return Optional.empty();
	}

	@Override
	public boolean isBooleanAttribute()
	{
		return false;
	}
}
