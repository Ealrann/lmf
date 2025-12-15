package org.logoce.lmf.core.loader.linking.tree;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.linking.ModelGroup;
import org.logoce.lmf.core.loader.internal.interpretation.PFeature;
import org.logoce.lmf.core.api.text.syntax.PNode;

import java.util.List;

public record LinkInfo<T extends LMObject, I extends PNode>(I pNode,
															Relation<T, ?, ?, ?> containingRelation,
															List<PFeature> features,
															ModelGroup<T> modelGroup)
{
}
