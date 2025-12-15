package org.logoce.lmf.core.loader.model;

import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.linking.LinkNode;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.List;

public record LmDocument(Model model,
						 List<LmDiagnostic> diagnostics,
						 List<Tree<PNode>> roots,
						 CharSequence source,
						 List<? extends LinkNode<?, PNode>> linkTrees)
{
}
