package org.logoce.lmf.model.loader.model;

import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;

public record LmDocument(Model model,
						 List<LmDiagnostic> diagnostics,
						 List<Tree<PNode>> roots,
						 CharSequence source)
{
}

