package org.logoce.lmf.model.resource.interpretation;

import org.logoce.lmf.model.resource.parsing.PNode;

import java.util.List;

public record PGroup<I extends PNode>(I pnode, PType type, List<PFeature> features)
{}
