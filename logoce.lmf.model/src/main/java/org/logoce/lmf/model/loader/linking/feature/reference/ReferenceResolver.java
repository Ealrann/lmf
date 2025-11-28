package org.logoce.lmf.model.loader.linking.feature.reference;

import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.FeatureResolution;

import java.util.Optional;

public interface ReferenceResolver
{
	Optional<FeatureResolution<Relation<?, ?>>> resolve(PathParser path);
}

