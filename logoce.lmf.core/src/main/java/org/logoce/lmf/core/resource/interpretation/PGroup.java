package org.logoce.lmf.core.resource.interpretation;

import org.logoce.lmf.core.resource.parsing.PNode;

import java.util.List;

public record PGroup<I extends PNode>(I pnode, PType type, List<PFeature> features)
{}
