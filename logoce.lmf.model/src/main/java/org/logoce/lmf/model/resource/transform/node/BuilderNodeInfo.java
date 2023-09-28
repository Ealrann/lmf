package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;

public record BuilderNodeInfo<T extends LMObject>(Relation<T, ?> containingRelation,
												  List<String> words,
												  ModelGroup<T> modelGroup) {}
