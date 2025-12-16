package org.logoce.lmf.core.loader.internal.interpretation;

import org.logoce.lmf.core.api.text.syntax.PNode;

import java.util.List;

public record PGroup<I extends PNode>(I pnode, PType type, List<PFeature> features)
{}
