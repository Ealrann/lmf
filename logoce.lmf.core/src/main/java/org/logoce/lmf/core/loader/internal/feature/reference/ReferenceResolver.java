package org.logoce.lmf.core.loader.internal.feature.reference;

import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.linking.FeatureResolution;

import java.util.Optional;

public interface ReferenceResolver
{
	Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolve(PathParser path);
}
