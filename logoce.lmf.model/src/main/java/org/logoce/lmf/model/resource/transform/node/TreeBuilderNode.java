package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.extender.api.IAdapterDescriptorRegistry;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.adapter.LexerTokenAdapter;
import org.logoce.lmf.model.resource.parsing.ParsedToken;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;
import org.logoce.lmf.model.resource.transform.word.resolver.TokenResolver;
import org.logoce.lmf.model.util.AbstractTree;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class TreeBuilderNode<T extends LMObject> extends AbstractTree<BuilderNodeInfo<T>, TreeBuilderNode<T>>
{
	private final IFeaturedObject.Builder<T> builder;
	private final boolean setTokenAdapter;

	private List<? extends IFeatureResolution> tokenResolutions;
	private List<NoSuchElementException> resolutionErrors;

	public T builtObject = null;

	public TreeBuilderNode(final BuildInfo<BuilderNodeInfo<T>, TreeBuilderNode<T>> info,
						   final boolean setTokenAdapter)
	{
		super(info);
		this.setTokenAdapter = setTokenAdapter;
		builder = data().modelGroup().builder();
		children().forEach(this::injectContainment);
	}

	private void injectContainment(final TreeBuilderNode<T> child)
	{
		builder.push(child.containingRelation(), child::build);
	}

	public Relation<T, ?> containingRelation()
	{
		return data().containingRelation();
	}

	public List<ParsedToken> tokens()
	{
		return data().tokens();
	}

	public ModelGroup<T> modelGroup()
	{
		return data().modelGroup();
	}

	public void setResolutions(List<TokenResolver.ResolutionAptempt> tokenResolutions)
	{
		this.tokenResolutions = tokenResolutions.stream()
												.map(TokenResolver.ResolutionAptempt::resolution)
												.filter(Objects::nonNull)
												.toList();
		this.resolutionErrors = tokenResolutions.stream()
												.map(TokenResolver.ResolutionAptempt::exception)
												.filter(Objects::nonNull)
												.toList();
	}

	public T build()
	{
		if (builtObject == null)
		{
			tokenResolutions.forEach(r -> r.pushValue(builder));
			builtObject = builder.build();

			if(setTokenAdapter)
			{
				final var test= IAdapterDescriptorRegistry.INSTANCE;
				final var lexerTokenAdapter = builtObject.adapt(LexerTokenAdapter.class);
				lexerTokenAdapter.storeTokens(data.tokens());
				lexerTokenAdapter.storeErrors(resolutionErrors);
			}
			else
			{
				for (final var error : resolutionErrors)
				{
					error.printStackTrace();
				}
			}
		}
		return builtObject;
	}
}
