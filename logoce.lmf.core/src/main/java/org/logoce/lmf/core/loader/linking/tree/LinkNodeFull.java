package org.logoce.lmf.core.loader.linking.tree;

import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.linking.LinkException;
import org.logoce.lmf.core.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.internal.linking.NodeLinker;
import org.logoce.lmf.core.loader.internal.interpretation.PFeature;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.DynamicModelPackage;
import org.logoce.lmf.core.util.tree.AbstractTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class LinkNodeFull<T extends LMObject, I extends PNode> extends AbstractTree<LinkNodeFull<?, I>> implements
																											  LinkNodeInternal<T, I, LinkNodeFull<?, I>>
{
	private final LinkInfo<T, I> info;
	private final IFeaturedObject.Builder<T> builder;

	private final List<ResolutionAttempt<Attribute<?, ?, ?, ?>>> attributeResolutions;
	private List<ResolutionAttempt<Relation<?, ?, ?, ?>>> relationResolutions;
	private T builtObject = null;
	private IModelPackage injectedMetaModelPackage = null;

	private static final ThreadLocal<Set<LinkNodeFull<?, ?>>> BUILD_STACK = ThreadLocal.withInitial(HashSet::new);

	public LinkNodeFull(final LinkInfo<T, I> info,
						final LinkNodeFull<?, I> parent,
						final List<ResolutionAttempt<Attribute<?, ?, ?, ?>>> attributeResolutions,
						final Function<LinkNodeFull<?, I>, List<LinkNodeFull<?, I>>> childrenBuilder)
	{
		super(parent, childrenBuilder);
		this.info = info;
		this.builder = info.modelGroup().builder();
		this.attributeResolutions = List.copyOf(attributeResolutions);
	}

	@Override
	public void resolveReferences(final NodeLinker nodeLinker)
	{
		this.relationResolutions = nodeLinker.resolveRelations(this);
	}

	@Override
	public List<ResolutionAttempt<Attribute<?, ?, ?, ?>>> attributeResolutions()
	{
		return attributeResolutions;
	}

	@Override
	public List<ResolutionAttempt<Relation<?, ?, ?, ?>>> relationResolutions()
	{
		return relationResolutions;
	}

	@Override
	public I pNode()
	{
		return info.pNode();
	}

	@Override
	public LinkNodeFull<?, I> root()
	{
		return super.root();
	}

	@Override
	public T build()
	{
		if (builtObject == null)
		{
			final var stack = BUILD_STACK.get();
			if (stack.contains(this))
			{
				throw new LinkException("Cyclic model containment while building group '" +
										group().name() +
										"' near token '" +
										firstToken() +
										"'", pNode());
			}
			stack.add(this);

			try
			{
				injectMetaModelPackageIfNeeded();

				streamChildren().forEach(this::injectContainment);
				attributeResolutions.forEach(this::install);
				relationResolutions.forEach(this::install);

				builtObject = builder.build();
				bindInjectedMetaModelPackageIfNeeded();
			}
			catch (LinkException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				final String message;
				final var baseMessage = e.getMessage();

				// Provide a more user-friendly message when a mandatory feature
				// is missing and the underlying builder fails with a null-based
				// error (for example, AttributeBuilder missing its 'datatype').
				if (e instanceof NullPointerException &&
					baseMessage != null &&
					baseMessage.contains("this.datatype"))
				{
					message = "Mandatory feature \"datatype\" is not defined";
				}
				else
				{
					message = (baseMessage == null || baseMessage.isBlank())
							  ? "Link error while building group '" + group().name() + "'"
							  : baseMessage;
				}

				throw new LinkException(message, pNode());
			}
			finally
			{
				stack.remove(this);
				if (stack.isEmpty())
				{
					BUILD_STACK.remove();
				}
			}
		}
		return builtObject;
	}

	private void injectMetaModelPackageIfNeeded()
	{
		if (builder instanceof MetaModel.Builder metaModelBuilder)
		{
			final var dynamicPackage = DynamicModelPackage.unbound();
			this.injectedMetaModelPackage = dynamicPackage;
			metaModelBuilder.lmPackage(dynamicPackage);
		}
	}

	private void bindInjectedMetaModelPackageIfNeeded()
	{
		if (injectedMetaModelPackage instanceof DynamicModelPackage dynamic &&
			builtObject instanceof MetaModel metaModel)
		{
			dynamic.bind(metaModel);
		}
	}

	private void install(final ResolutionAttempt<?> tokenResolution)
	{
		if (tokenResolution.resolution() != null)
		{
			pushValue(tokenResolution.resolution());
		}
		if (tokenResolution.exception() != null)
		{
			throw buildLinkException(tokenResolution);
		}
	}

	private LinkException buildLinkException(final ResolutionAttempt<?> tokenResolution)
	{
		final PFeature feature = tokenResolution.feature();
		final var token = feature.firstToken();
		final var groupName = group().name();
		final var baseMessage = tokenResolution.exception().getMessage();
		final var message = (baseMessage == null || baseMessage.isBlank())
							? "Cannot resolve token '" + token + "' in group '" + groupName + "'"
							: baseMessage + " in group '" + groupName + "' for token '" + token + "'";
		return new LinkException(message, pNode());
	}

	private void injectContainment(final LinkNodeFull<?, I> child)
	{
		child.injectContainmentInto(builder);
	}

	private void injectContainmentInto(final IFeaturedObject.Builder<?> otherBuilder)
	{
		otherBuilder.push(containingRelation(), this::build);
	}

	private void pushValue(final FeatureResolution<?> resolution)
	{
		resolution.pushValue(builder);
	}

	@Override
	public Relation<T, ?, ?, ?> containingRelation()
	{
		return info.containingRelation();
	}

	@Override
	public List<PFeature> features()
	{
		return info.features();
	}

	@Override
	public Group<T> group()
	{
		return info.modelGroup().group();
	}

	private String firstToken()
	{
		final var tokens = pNode().tokens();
		return tokens.isEmpty() ? group().name() : tokens.getFirst().value();
	}
}
