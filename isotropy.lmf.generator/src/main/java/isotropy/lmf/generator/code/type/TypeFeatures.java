package isotropy.lmf.generator.code.type;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.util.TypeParameter;

import java.util.List;

public record TypeFeatures(Group<?> group, TypeParameter interfaceType, List<FeatureResolution> resolutions) {}
