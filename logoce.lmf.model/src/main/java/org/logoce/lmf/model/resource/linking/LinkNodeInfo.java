package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;

import java.util.List;

public record LinkNodeInfo<T extends LMObject>(Relation<T, ?> containingRelation,
											   List<PFeature> features,
											   ModelGroup<T> modelGroup) {}
