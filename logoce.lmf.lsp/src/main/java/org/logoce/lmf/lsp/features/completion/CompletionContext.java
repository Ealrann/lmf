package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;

import java.net.URI;

public record CompletionContext(
	LmLanguageServer server,
	URI uri,
	Position position,
	LmDocumentState documentState,
	SyntaxSnapshot syntax,
	SemanticSnapshot semantic,
	MetaModel metaModel,
	CompletionContextKind contextKind,
	HeaderContext header,
	ValueContext value)
{
	public enum HeaderPositionKind
	{
		OTHER,
		HEADER_KEYWORD,
		HEADER_NAME,
		FEATURE_NAME,
		FEATURE_VALUE
	}

	public record HeaderContext(String keyword,
								String groupName,
								Group<?> lmCoreGroup,
								HeaderPositionKind positionKind,
								Group<?> semanticGroup,
								Feature<?, ?> semanticFeature,
								String featureName)
	{
	}

	public record ValueContext(Attribute<?, ?> attribute,
							   Relation<?, ?> relation,
							   Concept<?> relationConcept,
							   TypeUsageKind typeUsageKind)
	{
	}
}
