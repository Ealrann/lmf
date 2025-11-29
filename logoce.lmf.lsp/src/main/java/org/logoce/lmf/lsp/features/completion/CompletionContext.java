package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.MetaModel;

import java.net.URI;

public record CompletionContext(
	LmLanguageServer server,
	URI uri,
	Position position,
	LmDocumentState documentState,
	SyntaxSnapshot syntax,
	SemanticSnapshot semantic,
	MetaModel metaModel,
	CompletionContextKind contextKind)
{
}

