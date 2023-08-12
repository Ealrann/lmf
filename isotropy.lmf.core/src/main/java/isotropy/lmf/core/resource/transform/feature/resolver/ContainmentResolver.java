package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.util.BuilderNode;

import java.util.Optional;

public final class ContainmentResolver<T extends LMObject> extends AbstractResolver<T, Relation<T, ?>> implements
																									   IChildResolver<T>
{
	public ContainmentResolver(final Relation<T, ?> relation)
	{
		super(relation);
		assert relation.contains();
	}

	@Override
	public Optional<IFeatureResolution> resolve(BuilderNode node)
	{
		return Optional.empty();
	}
}
