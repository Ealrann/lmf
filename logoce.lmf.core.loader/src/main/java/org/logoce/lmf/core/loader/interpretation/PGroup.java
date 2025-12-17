package org.logoce.lmf.core.loader.interpretation;

import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.List;

public record PGroup<I extends PNode>(I pnode, PType type, List<PFeature> features)
{}
