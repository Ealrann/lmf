package org.logoce.lmf.core.loader.api.loader.linking.tree;

import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.api.loader.linking.AmbiguousReferenceException;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidEnumLiteralException;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidReferenceException;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidUnitLiteralException;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;
import org.logoce.lmf.core.loader.api.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.linking.NodeLinker;
import org.logoce.lmf.core.loader.interpretation.PFeature;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.api.model.DynamicModelPackage;
import org.logoce.lmf.core.util.tree.AbstractTree;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
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
		final var exception = tokenResolution.exception();
		final var message = buildLinkMessage(feature, groupName, token, exception);
		final var highlightToken = exception instanceof InvalidEnumLiteralException invalid
								   ? locateValueToken(pNode(), invalid.featureName(), invalid.literal())
								   : exception instanceof InvalidUnitLiteralException invalid
									 ? locateValueToken(pNode(), invalid.featureName(), invalid.literal())
									 : exception instanceof InvalidReferenceException invalid
									   ? locateValueToken(pNode(), invalid.featureName(), invalid.rawReference())
									   : exception instanceof AmbiguousReferenceException ambiguous
										 ? locateValueToken(pNode(), ambiguous.featureName(), ambiguous.rawReference())
										 : shouldHighlightReferenceFailure(feature, exception)
										   ? locateValueToken(pNode(), feature.name().orElse(null), firstValue(feature))
										 : null;
		return new LinkException(message, pNode(), highlightToken);
	}

	private String buildLinkMessage(final PFeature feature,
									final String groupName,
									final String token,
									final Exception exception)
	{
		if (exception instanceof InvalidReferenceException invalid)
		{
			return buildInvalidReferenceMessage(groupName, invalid);
		}
		if (exception instanceof AmbiguousReferenceException ambiguous)
		{
			return buildAmbiguousReferenceMessage(groupName, ambiguous);
		}

		final var baseMessage = exception == null ? null : exception.getMessage();
		if (feature != null && feature.isRelation() && feature.name().isPresent() && !feature.values().isEmpty())
		{
			final var featureName = feature.name().get();
			final var resolvedRelation = resolveRelationByName(featureName);
			if (resolvedRelation != null)
			{
				final var rawReference = firstValue(feature);
				final var expected = expectedConceptName(resolvedRelation);
				final var base = "Cannot resolve reference '" +
								 rawReference +
								 "' for relation '" +
								 featureName +
								 "'" +
								 (expected == null ? "" : " (expected " + expected + ")") +
								 " in group '" +
								 groupName +
								 "'";

				if (baseMessage == null || baseMessage.isBlank())
				{
					return base;
				}
				if (baseMessage.startsWith("Cannot resolve named Token ") || baseMessage.startsWith("Cannot resolve value Token "))
				{
					return base;
				}
				return base + ": " + baseMessage;
			}
		}

		return (baseMessage == null || baseMessage.isBlank())
			   ? "Cannot resolve token '" + token + "' in group '" + groupName + "'"
			   : baseMessage + " in group '" + groupName + "' for token '" + token + "'";
	}

	private static String buildInvalidReferenceMessage(final String groupName,
													  final InvalidReferenceException invalid)
	{
		final var raw = invalid.rawReference();
		final var featureName = invalid.featureName();
		final var expected = invalid.expectedConceptName();
		final var actual = invalid.actualConceptName();

		return "Cannot resolve reference '" +
			   raw +
			   "' for relation '" +
			   featureName +
			   "'" +
			   (expected == null || expected.isBlank() ? "" : actual == null || actual.isBlank()
													   ? " (expected " + expected + ")"
													   : " (expected " + expected + " but found " + actual + ")") +
			   " in group '" +
			   groupName +
			   "'";
	}

	private static String buildAmbiguousReferenceMessage(final String groupName,
														final AmbiguousReferenceException ambiguous)
	{
		final var raw = ambiguous.rawReference();
		final var featureName = ambiguous.featureName();
		final var expected = ambiguous.expectedConceptName();
		final var matchCount = ambiguous.matchCount();

		return "Ambiguous reference '" +
			   raw +
			   "' for relation '" +
			   featureName +
			   "'" +
			   (expected == null || expected.isBlank() ? "" : " (expected " + expected + ")") +
			   " in group '" +
			   groupName +
			   "' (" +
			   matchCount +
			   " matches)";
	}

	private Relation<?, ?, ?, ?> resolveRelationByName(final String featureName)
	{
		if (featureName == null || featureName.isBlank())
		{
			return null;
		}

		return ModelUtil.streamAllFeatures(group())
						.filter(f -> featureName.equals(f.name()))
						.filter(Relation.class::isInstance)
						.map(Relation.class::cast)
						.findFirst()
						.orElse(null);
	}

	private static boolean shouldHighlightReferenceFailure(final PFeature feature, final Exception exception)
	{
		if (feature == null || exception == null)
		{
			return false;
		}
		if (!feature.isRelation() || feature.name().isEmpty() || feature.values().isEmpty())
		{
			return false;
		}
		return exception instanceof NoSuchElementException;
	}

	private static String firstValue(final PFeature feature)
	{
		if (feature == null || feature.values() == null || feature.values().isEmpty())
		{
			return "";
		}
		final var value = feature.values().getFirst();
		return value == null ? "" : value;
	}

	private static String expectedConceptName(final Relation<?, ?, ?, ?> relation)
	{
		if (relation == null)
		{
			return null;
		}
		final var concept = relation.concept();
		return concept == null ? null : concept.name();
	}

	private static PToken locateValueToken(final PNode node, final String featureName, final String rawValue)
	{
		if (node == null || rawValue == null)
		{
			return null;
		}

		final var tokens = node.tokens();
		if (tokens == null || tokens.isEmpty())
		{
			return null;
		}

		if (featureName != null)
		{
			for (int i = 0; i < tokens.size(); i++)
			{
				final var token = tokens.get(i);
				if (token.type() != ELMTokenType.VALUE_NAME || !featureName.equals(token.value()))
				{
					continue;
				}

				int cursor = skipWhitespace(tokens, i + 1);
				if (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.ASSIGN)
				{
					cursor = skipWhitespace(tokens, cursor + 1);
				}

				for (int j = cursor; j < tokens.size(); j++)
				{
					final var current = tokens.get(j);
					if (current.type() == ELMTokenType.VALUE && rawValue.equals(current.value()))
					{
						return current;
					}

					if (current.type() == ELMTokenType.WHITE_SPACE)
					{
						final int next = skipWhitespace(tokens, j + 1);
						if (next < tokens.size() && tokens.get(next).type() == ELMTokenType.VALUE_NAME)
						{
							break;
						}
					}
				}
			}
		}

		for (final var token : tokens)
		{
			if (token.type() == ELMTokenType.VALUE && rawValue.equals(token.value()))
			{
				return token;
			}
		}

		return null;
	}

	private static int skipWhitespace(final List<PToken> tokens, final int start)
	{
		int cursor = start;
		while (cursor < tokens.size() && tokens.get(cursor).type() == ELMTokenType.WHITE_SPACE)
		{
			cursor++;
		}
		return cursor;
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
