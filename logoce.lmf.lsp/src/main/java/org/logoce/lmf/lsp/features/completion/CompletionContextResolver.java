package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Datatype;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;

/**
 * Central resolver for {@link CompletionContext}. This is the single entry point
 * responsible for:
 * <ul>
 *     <li>Locating the current {@link LmDocumentState}.</li>
 *     <li>Choosing between current and last-good {@link SemanticSnapshot}.</li>
 *     <li>Resolving the active {@link MetaModel}.</li>
 *     <li>Classifying the completion context kind (plain, '@', '#').</li>
 * </ul>
 * <p>
 * Higher-level code (completion providers and the engine) should avoid re-implementing
 * these decisions and instead rely on the information exposed by the context.
 */
final class CompletionContextResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(CompletionContextResolver.class);

	private CompletionContextResolver()
	{
	}

	static Optional<CompletionContext> resolve(final LmLanguageServer server,
											   final URI uri,
											   final Position pos)
	{
		final LmDocumentState state = server.workspaceIndex().getDocument(uri);
		if (state == null)
		{
			LOG.debug("LMF LSP completion: no document state for uri={}", uri);
			return Optional.empty();
		}

		final SyntaxSnapshot syntax = state.syntaxSnapshot();
		final SemanticSnapshot semantic = state.semanticSnapshot() != null
										  ? state.semanticSnapshot()
										  : state.lastGoodSemanticSnapshot();
		if (syntax == null || semantic == null)
		{
			LOG.debug("LMF LSP completion: missing snapshots for uri={}, syntaxNull={}, semanticNull={}",
					  uri, syntax == null, semantic == null);
			return Optional.empty();
		}

		final Model model = semantic.model();
		final ModelRegistry registry = server.workspaceIndex().modelRegistry();
		final MetaModel metaModel = MetaModelResolver.resolveForDocument(syntax, model, registry);
		final CompletionContextKind contextKind = SyntaxNavigation.detectCompletionContext(syntax, pos);

		// Header / value context derived from the current line and the active meta-model / LMCore.
		final HeaderInfo headerInfo = HeaderInfo.from(syntax, pos, metaModel);
		final CompletionContext.HeaderContext headerContext = buildHeaderContext(headerInfo, semantic, syntax, pos);
		final CompletionContext.ValueContext valueContext = buildValueContext(headerInfo, headerContext);

		final var context = new CompletionContext(server,
												 uri,
												 pos,
												 state,
												 syntax,
												 semantic,
												 metaModel,
												 contextKind,
												 headerContext,
												 valueContext);
		return Optional.of(context);
	}

	private static CompletionContext.HeaderContext buildHeaderContext(final HeaderInfo headerInfo,
																	  final SemanticSnapshot semantic,
																	  final SyntaxSnapshot syntax,
																	  final Position pos)
	{
		if (headerInfo == null)
		{
			return null;
		}

		Group<?> semanticGroup = null;
		Feature<?, ?> semanticFeature = null;

		if (semantic != null)
		{
			// Best-effort semantic group at the current position.
			semanticGroup = SemanticNavigation.findGroupAtPosition(semantic, syntax, pos);

			// For value positions inside a header, resolve the feature as well.
			if (headerInfo.positionKind() == CompletionContext.HeaderPositionKind.FEATURE_VALUE)
			{
				final var feature = SemanticNavigation.findFeatureAtValuePosition(semantic, syntax, pos);
				if (feature != null)
				{
					semanticFeature = feature;
				}
			}
		}

		return new CompletionContext.HeaderContext(
			headerInfo.keyword(),
			headerInfo.groupName(),
			headerInfo.headerGroup(),
			headerInfo.positionKind(),
			semanticGroup,
			semanticFeature,
			headerInfo.featureName());
	}

	private static CompletionContext.ValueContext buildValueContext(final HeaderInfo headerInfo,
																	final CompletionContext.HeaderContext headerContext)
	{
		if (headerInfo == null || headerContext == null ||
			headerContext.positionKind() != CompletionContext.HeaderPositionKind.FEATURE_VALUE)
		{
			return new CompletionContext.ValueContext(null, null, null, TypeUsageKind.ANY);
		}

		Group<?> headerGroup = headerContext.headerGroup();
		if (headerGroup == null)
		{
			headerGroup = headerContext.semanticGroup();
		}
		final String featureName = headerContext.featureName();

		if (headerGroup == null || featureName == null || featureName.isBlank())
		{
			return new CompletionContext.ValueContext(null, null, null, TypeUsageKind.ANY);
		}

		Attribute<?, ?> valueAttribute = null;
		Relation<?, ?> valueRelation = null;
		Concept<?> relationConcept = null;

		for (final var f : headerGroup.features())
		{
			if (featureName.equals(f.name()))
			{
				if (f instanceof Attribute<?, ?> a)
				{
					valueAttribute = a;
				}
				else if (f instanceof Relation<?, ?> r)
				{
					valueRelation = r;
					relationConcept = r.concept();
				}
				break;
			}
		}

		final TypeUsageKind typeUsageKind = resolveTypeUsageKind(headerInfo, valueAttribute, valueRelation, relationConcept);
		return new CompletionContext.ValueContext(valueAttribute, valueRelation, relationConcept, typeUsageKind);
	}

	private static TypeUsageKind resolveTypeUsageKind(final HeaderInfo headerInfo,
													  final Attribute<?, ?> valueAttribute,
													  final Relation<?, ?> valueRelation,
													  final Concept<?> relationConcept)
	{
		if (valueRelation != null && relationConcept != null)
		{
			final Group<?> conceptGroup = relationConcept.lmGroup();

			if (relationConcept instanceof Datatype<?> ||
				ModelUtils.isSubGroup(LMCoreModelDefinition.Groups.DATATYPE, conceptGroup))
			{
				return TypeUsageKind.DATATYPE;
			}

			if (ModelUtils.isSubGroup(LMCoreModelDefinition.Groups.CONCEPT, conceptGroup) ||
				ModelUtils.isSubGroup(LMCoreModelDefinition.Groups.TYPE, conceptGroup))
			{
				return TypeUsageKind.CONCEPT;
			}
		}

		if (valueAttribute != null && valueAttribute.datatype() instanceof Datatype<?>)
		{
			return TypeUsageKind.DATATYPE;
		}

		return TypeUsageKind.ANY;
	}

	/**
	 * Minimal header/value information extracted from the current line and the active
	 * meta-model / LMCore.
	 * The header is parsed regardless of whether an '=' exists; feature/value
	 * information is only present when the caret is within a feature.
	 */
	record HeaderInfo(String keyword,
					  String groupName,
					  Group<?> headerGroup,
					  String featureName,
					  CompletionContext.HeaderPositionKind positionKind)
	{
		static HeaderInfo from(final SyntaxSnapshot syntax, final Position pos)
		{
			return from(syntax, pos, null);
		}

		static HeaderInfo from(final SyntaxSnapshot syntax,
							   final Position pos,
							   final MetaModel activeMetaModel)
		{
			final HeaderInfo direct = fromLine(syntax, pos.getLine(), pos.getCharacter(), activeMetaModel);
			if (direct != null)
			{
				return direct;
			}

			// If the caret is on a line without a header but the previous line
			// contains it (e.g. "(Group Test\n<caret> ..."), treat this as a
			// continuation of that header and classify positions after the
			// group name as feature-name positions.
			if (pos.getLine() <= 0)
			{
				return null;
			}

			final int previousLine = pos.getLine() - 1;
			final CharSequence source = syntax.source();

			int line = 0;
			int lineStartOffset = -1;
			for (int i = 0; i < source.length(); i++)
			{
				final char c = source.charAt(i);
				if (line == previousLine)
				{
					lineStartOffset = i;
					break;
				}
				if (c == '\n')
				{
					line++;
				}
			}

			if (lineStartOffset == -1)
			{
				return null;
			}

			int lineEndOffset = source.length();
			for (int i = lineStartOffset; i < source.length(); i++)
			{
				if (source.charAt(i) == '\n')
				{
					lineEndOffset = i;
					break;
				}
			}

			final int lineLength = lineEndOffset - lineStartOffset;
			return fromLine(syntax, previousLine, lineLength, activeMetaModel);
		}

		private static HeaderInfo fromLine(final SyntaxSnapshot syntax,
										   final int targetLine,
										   final int targetChar,
										   final MetaModel activeMetaModel)
		{
			final CharSequence source = syntax.source();
			int line = 0;
			int lineStartOffset = -1;
			for (int i = 0; i < source.length(); i++)
			{
				final char c = source.charAt(i);
				if (line == targetLine)
				{
					lineStartOffset = i;
					break;
				}
				if (c == '\n')
				{
					line++;
				}
			}

			if (lineStartOffset == -1)
			{
				return null;
			}

			int lineEndOffset = source.length();
			for (int i = lineStartOffset; i < source.length(); i++)
			{
				if (source.charAt(i) == '\n')
				{
					lineEndOffset = i;
					break;
				}
			}

			final String lineText = source.subSequence(lineStartOffset, lineEndOffset).toString();

			final int openIdx = lineText.indexOf('(');
			if (openIdx < 0)
			{
				return null;
			}

			int closeIdx = lineText.indexOf(')', openIdx + 1);
			if (closeIdx < 0)
			{
				closeIdx = lineText.length();
			}

			final int headerStart = openIdx + 1;
			final int headerEnd = closeIdx;
			if (headerStart >= headerEnd)
			{
				return null;
			}

			final String header = lineText.substring(headerStart, headerEnd);
			final int headerCaret = Math.min(Math.max(0, targetChar - headerStart), header.length());

			final var tokens = AttributeValueCompletionProvider.tokenizeHeader(header);
			if (tokens.isEmpty())
			{
				return null;
			}

			final String keyword = tokens.getFirst().text();
			if (keyword == null || keyword.isBlank())
			{
				return null;
			}

			final String featureName = AttributeValueCompletionProvider.resolveFeatureNameAtCaret(tokens, headerCaret);

			final Group<?> headerGroup = resolveHeaderGroup(keyword, activeMetaModel);
			final String groupName = headerGroup != null ? headerGroup.name() : null;

			final CompletionContext.HeaderPositionKind positionKind =
				determinePositionKind(tokens, headerCaret, featureName);

			return new HeaderInfo(keyword, groupName, headerGroup, featureName, positionKind);
		}

		private static Group<?> resolveHeaderGroup(final String keyword, final MetaModel activeMetaModel)
		{
			if (keyword == null || keyword.isBlank())
			{
				return null;
			}

			// 1) Direct group name in the active meta-model.
			if (activeMetaModel != null)
			{
				for (final Group<?> g : activeMetaModel.groups())
				{
					if (keyword.equals(g.name()))
					{
						return g;
					}
				}
			}

			// 2) Direct group name in LMCore (MetaModel, Group, Definition, Enum, Unit, JavaWrapper, Alias, Attribute, Relation, ...).
			final MetaModel lmCore = LMCoreModelPackage.MODEL;
			for (final Group<?> g : lmCore.groups())
			{
				if (keyword.equals(g.name()))
				{
					return g;
				}
			}

			// 3) Alias-based header, e.g. +att / -att / +contains / -contains / Definition.
			final String targetGroupName = resolveGroupNameFromAliases(keyword, activeMetaModel, lmCore);
			if (targetGroupName == null || targetGroupName.isBlank())
			{
				return null;
			}

			// 4) Resolve the group name from aliases in the active meta-model first, then LMCore.
			if (activeMetaModel != null)
			{
				for (final Group<?> g : activeMetaModel.groups())
				{
					if (targetGroupName.equals(g.name()))
					{
						return g;
					}
				}
			}

			for (final Group<?> g : lmCore.groups())
			{
				if (targetGroupName.equals(g.name()))
				{
					return g;
				}
			}

			return null;
		}

		private static String resolveGroupNameFromAliases(final String keyword,
														  final MetaModel activeMetaModel,
														  final MetaModel lmCore)
		{
			// Prefer aliases defined in the active meta-model when present.
			if (activeMetaModel != null)
			{
				for (final org.logoce.lmf.model.lang.Alias alias : activeMetaModel.aliases())
				{
					if (keyword.equals(alias.name()))
					{
						return firstToken(alias.value());
					}
				}
			}

			for (final org.logoce.lmf.model.lang.Alias alias : lmCore.aliases())
			{
				if (keyword.equals(alias.name()))
				{
					return firstToken(alias.value());
				}
			}

			return null;
		}

		private static String firstToken(final String value)
		{
			if (value == null || value.isBlank())
			{
				return null;
			}

			int i = 0;
			final int len = value.length();
			while (i < len && Character.isWhitespace(value.charAt(i)))
			{
				i++;
			}
			final int start = i;
			while (i < len && !Character.isWhitespace(value.charAt(i)))
			{
				i++;
			}
			return start < i ? value.substring(start, i) : null;
		}

		private static CompletionContext.HeaderPositionKind determinePositionKind(final java.util.List<AttributeValueCompletionProvider.HeaderToken> tokens,
																				  final int headerCaret,
																				  final String featureName)
		{
			if (tokens.isEmpty())
			{
				return CompletionContext.HeaderPositionKind.OTHER;
			}

			final var first = tokens.getFirst();
			final var second = tokens.size() > 1 ? tokens.get(1) : null;

			if (headerCaret >= first.start() && headerCaret <= first.end())
			{
				return CompletionContext.HeaderPositionKind.HEADER_KEYWORD;
			}

			if (second != null && headerCaret >= second.start() && headerCaret <= second.end())
			{
				return CompletionContext.HeaderPositionKind.HEADER_NAME;
			}

			if (featureName == null || featureName.isBlank())
			{
				// Caret is not on the keyword or group/name token and no
				// feature assignment has been identified yet. When it sits
				// after the header name inside the header, treat this as
				// starting a new feature name so that header feature
				// completions are offered, e.g. "(Group Test <caret>)".
				if (second != null && headerCaret > second.end())
				{
					return CompletionContext.HeaderPositionKind.FEATURE_NAME;
				}

				return CompletionContext.HeaderPositionKind.OTHER;
			}

			// Reconstruct a simple view for the current feature: locate the '=' that
			// follows the feature name, then treat positions before it as FEATURE_NAME
			// and after it as FEATURE_VALUE.
			for (int i = 0; i < tokens.size(); i++)
			{
				final var tok = tokens.get(i);
				if (!featureName.equals(tok.text()))
				{
					continue;
				}

				final var eqIdx = (i + 1 < tokens.size() && "=".equals(tokens.get(i + 1).text()))
								  ? i + 1
								  : -1;
				if (eqIdx == -1)
				{
					continue;
				}

				final var eqToken = tokens.get(eqIdx);
				if (headerCaret <= eqToken.start())
				{
					return CompletionContext.HeaderPositionKind.FEATURE_NAME;
				}
				else
				{
					return CompletionContext.HeaderPositionKind.FEATURE_VALUE;
				}
			}

			return CompletionContext.HeaderPositionKind.OTHER;
		}
	}
}
