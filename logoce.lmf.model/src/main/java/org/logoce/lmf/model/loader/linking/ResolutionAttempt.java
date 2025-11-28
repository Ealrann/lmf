package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.resource.interpretation.PFeature;

import java.util.NoSuchElementException;

public record ResolutionAttempt<T extends Feature<?, ?>>(PFeature feature,
														 FeatureResolution<T> resolution,
														 NoSuchElementException exception)
{
}

