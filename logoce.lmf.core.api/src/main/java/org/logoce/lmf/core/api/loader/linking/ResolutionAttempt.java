package org.logoce.lmf.core.api.loader.linking;

import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.loader.internal.interpretation.PFeature;

import java.util.NoSuchElementException;

public record ResolutionAttempt<T extends Feature<?, ?, ?, ?>>(PFeature feature,
															   FeatureResolution<T> resolution,
															   NoSuchElementException exception)
{
}
