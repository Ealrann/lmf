package isotropy.lmf.core.api.feature;

import isotropy.lmf.core.lang.Feature;

import java.util.function.Supplier;

public record RawFeature<UnaryType, EffectiveType>(boolean many,
												   boolean relation,
												   Supplier<? extends Feature<UnaryType,
												   EffectiveType>> featureSupplier) {}
