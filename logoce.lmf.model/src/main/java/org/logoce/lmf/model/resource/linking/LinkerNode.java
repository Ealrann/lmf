package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;
import org.logoce.lmf.model.resource.transform.word.resolver.TokenResolver;
import org.logoce.lmf.model.util.AbstractTree;

import java.util.List;

public final class LinkerNode<T extends LMObject> extends AbstractTree<LinkNodeInfo<T>, LinkerNode<T>>
{
	private List<TokenResolver.ResolutionAptempt> tokenResolutions;

	public T builtObject = null;

	public LinkerNode(final BuildInfo<LinkNodeInfo<T>, LinkerNode<T>> info)
	{
		super(info);
	}

	public Relation<T, ?> containingRelation()
	{
		return data().containingRelation();
	}

	public List<PFeature> features()
	{
		return data().features();
	}

	public ModelGroup<T> modelGroup()
	{
		return data().modelGroup();
	}

	public void resolve(final TokenResolver tokenResolver)
	{
		this.tokenResolutions = tokenResolver.resolve(this);
	}

	public T build()
	{
		if (builtObject == null)
		{
			final var runner = BuildRunner.from(data().modelGroup());
			children().forEach(runner::injectContainment);

			for (final var tokenResolution : tokenResolutions)
			{
				if (tokenResolution.resolution() != null)
				{
					runner.pushValue(tokenResolution.resolution());
				}
				if (tokenResolution.exception() != null)
				{
					tokenResolution.exception().printStackTrace();
				}
			}

			builtObject = runner.build();
		}
		return builtObject;
	}

	private record BuildRunner<T extends LMObject>(IFeaturedObject.Builder<T> builder)
	{
		public static <T extends LMObject> BuildRunner<T> from(ModelGroup<T> modelGroup)
		{
			return new BuildRunner<>(modelGroup.builder());
		}

		public void injectContainment(final LinkerNode<T> child)
		{
			builder.push(child.containingRelation(), child::build);
		}

		public void pushValue(final IFeatureResolution resolution)
		{
			resolution.pushValue(builder);
		}

		public T build()
		{
			return builder.build();
		}
	}
}
