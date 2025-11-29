package org.logoce.lmf.lsp.state;

import java.net.URI;

public final class LmDocumentState
{
	private final URI uri;
	private int version;
	private String text;
	private SyntaxSnapshot syntaxSnapshot;
	private SemanticSnapshot semanticSnapshot;
	private SemanticSnapshot lastGoodSemanticSnapshot;

	public LmDocumentState(final URI uri, final int version, final String text)
	{
		this.uri = uri;
		this.version = version;
		this.text = text;
	}

	public URI uri()
	{
		return uri;
	}

	public int version()
	{
		return version;
	}

	public void setVersion(final int version)
	{
		this.version = version;
	}

	public String text()
	{
		return text;
	}

	public void setText(final String text)
	{
		this.text = text;
	}

	public SyntaxSnapshot syntaxSnapshot()
	{
		return syntaxSnapshot;
	}

	public void setSyntaxSnapshot(final SyntaxSnapshot syntaxSnapshot)
	{
		this.syntaxSnapshot = syntaxSnapshot;
	}

	public SemanticSnapshot semanticSnapshot()
	{
		return semanticSnapshot;
	}

	public void setSemanticSnapshot(final SemanticSnapshot semanticSnapshot)
	{
		this.semanticSnapshot = semanticSnapshot;
	}

	public SemanticSnapshot lastGoodSemanticSnapshot()
	{
		return lastGoodSemanticSnapshot;
	}

	public void setLastGoodSemanticSnapshot(final SemanticSnapshot lastGoodSemanticSnapshot)
	{
		this.lastGoodSemanticSnapshot = lastGoodSemanticSnapshot;
	}
}
