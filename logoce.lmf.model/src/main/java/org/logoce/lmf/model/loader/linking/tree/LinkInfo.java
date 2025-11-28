package org.logoce.lmf.model.loader.linking.tree;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.ModelGroup;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;

public record LinkInfo<T extends LMObject, I extends PNode>(I pNode,
															Relation<T, ?> containingRelation,
															List<PFeature> features,
															ModelGroup<T> modelGroup)
{
}

