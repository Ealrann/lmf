package org.logoce.lmf.model.resource.adapter;

import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.parsing.ParsedToken;

import java.util.List;
import java.util.NoSuchElementException;

@ModelExtender(scope = LMObject.class)
@Adapter
public class LexerTokenAdapter implements IAdapter
{
	private final LMObject adaptedObject;

	private List<ParsedToken> tokens = null;
	private List<NoSuchElementException> resolutionErrors;

	public LexerTokenAdapter(LMObject object)
	{
		adaptedObject = object;
	}

	public void storeTokens(List<ParsedToken> tokens)
	{
		this.tokens = List.copyOf(tokens);
	}

	public void storeErrors(List<NoSuchElementException> resolutionErrors)
	{
		this.resolutionErrors = List.copyOf(resolutionErrors);
	}

	public List<ParsedToken> getTokens()
	{
		return tokens;
	}
}
