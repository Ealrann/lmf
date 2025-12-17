package org.logoce.lmf.core.loader.api.loader.linking.tree;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.ModelGroup;
import org.logoce.lmf.core.loader.interpretation.PFeature;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.List;

public record LinkInfo<T extends LMObject, I extends PNode>(I pNode,
															Relation<T, ?, ?, ?> containingRelation,
															List<PFeature> features,
															ModelGroup<T> modelGroup)
{
}
