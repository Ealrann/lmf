package logoce.lmf.model.api.feature;

import logoce.lmf.model.lang.Feature;

import java.util.function.Supplier;

public record RawFeature<UnaryType, EffectiveType>(boolean many,
												   boolean relation,
												   Supplier<? extends Feature<UnaryType, EffectiveType>> featureSupplier) {}
