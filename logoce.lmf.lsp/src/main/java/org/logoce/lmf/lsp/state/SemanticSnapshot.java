package org.logoce.lmf.lsp.state;

import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.api.loader.linking.LinkNode;
import org.logoce.lmf.core.api.text.syntax.PNode;

import java.util.List;

public final class SemanticSnapshot
{
	private final Model model;
	private final List<? extends LinkNode<?, PNode>> linkTrees;
	private final List<LmDiagnostic> diagnostics;
	private final SymbolTable symbolTable;
	private final List<ReferenceOccurrence> references;

	public SemanticSnapshot(final Model model,
							final List<? extends LinkNode<?, PNode>> linkTrees,
							final List<LmDiagnostic> diagnostics,
							final SymbolTable symbolTable,
							final List<ReferenceOccurrence> references)
	{
		this.model = model;
		this.linkTrees = List.copyOf(linkTrees);
		this.diagnostics = List.copyOf(diagnostics);
		this.symbolTable = symbolTable;
		this.references = List.copyOf(references);
	}

	public Model model()
	{
		return model;
	}

	public List<? extends LinkNode<?, PNode>> linkTrees()
	{
		return linkTrees;
	}

	public List<LmDiagnostic> diagnostics()
	{
		return diagnostics;
	}

	public SymbolTable symbolTable()
	{
		return symbolTable;
	}

	public List<ReferenceOccurrence> references()
	{
		return references;
	}
}
