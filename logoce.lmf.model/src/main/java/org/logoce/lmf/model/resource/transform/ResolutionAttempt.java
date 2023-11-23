package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.FeatureResolution;

import java.util.NoSuchElementException;

public record ResolutionAttempt<T extends Feature<?, ?>>(PFeature feature,
													  FeatureResolution<T> resolution,
													  NoSuchElementException exception) {}
