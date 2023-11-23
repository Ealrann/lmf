package org.logoce.lmf.model.resource.linking.feature.reference;

import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.FeatureResolution;

import java.util.Optional;

public interface ReferenceResolver
{
	Optional<FeatureResolution<Relation<?, ?>>> resolve(final PathParser path);
}
