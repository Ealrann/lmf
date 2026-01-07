package org.logoce.lmf.cli.move;

import org.logoce.lmf.cli.edit.ReferenceEditPlanner;
import org.logoce.lmf.cli.edit.ReferenceStringUtil;
import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.cli.workspace.WorkspaceDocuments;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

public final class MovePlanner
{
	private static final String INDENT = "\t";
	private final ReferenceEditPlanner referencePlanner = new ReferenceEditPlanner();

	public MovePlanResult plan(final WorkspaceDocuments workspace,
						 final String fromReference,
						 final String toReference)
	{
		Objects.requireNonNull(workspace, "workspace");
		Objects.requireNonNull(fromReference, "fromReference");
		Objects.requireNonNull(toReference, "toReference");

		try
		{
			final var targetDocument = workspace.targetDocument();
			final var document = targetDocument.document();
			final var source = document.source();
			final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
			if (linkRoots.isEmpty())
			{
				throw new MovePlanException("No link trees available for target document");
			}

			final var fromNode = resolveNode(linkRoots, fromReference, "source reference");
			final var fromId = resolveObjectId(fromNode, "source object");
			if ("/".equals(fromId.path()))
			{
				throw new MovePlanException("Cannot move the model root");
			}

			final var subtreeSpan = SubtreeSpanLocator.locate(source, fromNode);
			if (subtreeSpan == null)
			{
				throw new MovePlanException("Cannot locate subtree span for " + fromReference);
			}

			final var movedNodes = collectNodes(fromNode);
			final var movedIds = collectObjectIds(movedNodes);

			final var slot = MoveSlotReference.parse(toReference);
			final var destParent = resolveNode(linkRoots, slot.parentReference(), "destination parent reference");
			if (movedNodes.contains(destParent))
			{
				throw new MovePlanException("Cannot move a node into its own subtree");
			}

			final var relation = resolveContainmentRelation(destParent, slot.featureName());
			if (relation == null)
			{
				throw new MovePlanException("Unknown containment relation '" + slot.featureName() + "' on " + slot.parentReference());
			}
			if (!relation.contains())
			{
				throw new MovePlanException("Relation '" + slot.featureName() + "' is not a containment");
			}
			if (!isCompatible(relation, fromNode))
			{
				throw new MovePlanException("Cannot move " + fromReference + " into '" + slot.featureName() + "' (incompatible types)");
			}

			final var destChildren = listChildren(destParent, relation.name());
			final OptionalInt destIndexOpt = slot.index();
			if (relation.many())
			{
				if (destIndexOpt.isEmpty())
				{
					throw new MovePlanException("Containment list '" + relation.name() + "' requires an index (for example '" + toReference + ".0')");
				}
				final int destIndex = destIndexOpt.getAsInt();
				if (destIndex < 0 || destIndex > destChildren.size())
				{
					throw new MovePlanException("Destination index out of range: " + destIndex + " (size=" + destChildren.size() + ")");
				}
			}
			else if (destIndexOpt.isPresent())
			{
				throw new MovePlanException("Containment '" + relation.name() + "' is not a list; remove the index and use '" + slot.parentReference() + "/" + relation.name() + "'");
			}

			final var sourceRelation = fromNode.containingRelation();
			final var sourceParent = fromNode.parent();
			if (sourceRelation == null || sourceParent == null)
			{
				throw new MovePlanException("Cannot resolve source containment for " + fromReference);
			}

			final var sourceParentId = resolveObjectId(sourceParent, "source parent");
			final var destParentId = resolveObjectId(destParent, "destination parent");
			if (!Objects.equals(fromId.modelQualifiedName(), destParentId.modelQualifiedName()))
			{
				throw new MovePlanException("Cannot move objects across models");
			}

			final OptionalInt sourceIndexOpt = sourceRelation.many()
																? OptionalInt.of(indexOfChild(sourceParent, sourceRelation.name(), fromNode))
																: OptionalInt.empty();

			final boolean sameSlot = !sourceRelation.many()
									 && !relation.many()
									 && Objects.equals(sourceRelation.name(), relation.name())
									 && Objects.equals(sourceParentId.path(), destParentId.path());

			final boolean sameList = sourceRelation.many()
												 && relation.many()
												 && Objects.equals(sourceRelation.name(), relation.name())
												 && Objects.equals(sourceParentId.path(), destParentId.path());

			if (sameSlot)
			{
				return new MovePlanResult.Success(new MovePlannedEdit(Map.of(), false));
			}

			if (sameList)
			{
				final int sourceIndex = sourceIndexOpt.getAsInt();
				final int destIndex = destIndexOpt.getAsInt();
				if (destIndex == sourceIndex || destIndex == sourceIndex + 1)
				{
					return new MovePlanResult.Success(new MovePlannedEdit(Map.of(), false));
				}
			}

			if (!relation.many() && !destChildren.isEmpty())
			{
				throw new MovePlanException("Containment '" + relation.name() + "' already has a value; use replace instead");
			}

			final var newRootPath = buildNewRootPath(destParentId.path(), relation, destIndexOpt, sameList, sourceIndexOpt);
			final var movedPaths = buildMovedPathMap(movedIds, fromId.path(), newRootPath);

			final var shifts = buildShiftContexts(fromId.modelQualifiedName(),
												 sourceParentId.path(),
												 sourceRelation,
												 sourceIndexOpt,
												 destParentId.path(),
												 relation,
												 destIndexOpt,
												 sameList);

			final ReferenceEditPlanner.ReferenceMapper mapper = (node, raw, targetId, quoted) -> rewriteReference(raw, targetId, movedPaths, shifts);

			final var editsByFile = new HashMap<Path, List<TextEdits.TextEdit>>();
			for (final var doc : workspace.documents())
			{
				final var roots = RootReferenceResolver.collectLinkRoots(doc.document().linkTrees());
				if (roots.isEmpty())
				{
					continue;
				}

				final var skip = doc.path().equals(targetDocument.path()) ? movedNodes : Set.<LinkNodeInternal<?, PNode, ?>>of();
				final var edits = referencePlanner.planEdits(doc.document().source(), roots, skip, mapper, 0);
				if (!edits.isEmpty())
				{
					editsByFile.computeIfAbsent(doc.path(), ignored -> new ArrayList<>()).addAll(edits);
				}
			}

			final var subtreeText = source.subSequence(subtreeSpan.startOffset(), subtreeSpan.endOffset()).toString();
			final var subtreeEdits = referencePlanner.planEdits(source,
												 List.of(fromNode),
												 Set.of(),
												 mapper,
												 -subtreeSpan.startOffset());
			final var updatedSubtree = TextEdits.apply(subtreeText, subtreeEdits);
			final var formattedSubtree = formatSubtree(updatedSubtree);

			final var removalEdit = new TextEdits.TextEdit(subtreeSpan.startOffset(), subtreeSpan.length(), "");
			editsByFile.computeIfAbsent(targetDocument.path(), ignored -> new ArrayList<>()).add(removalEdit);

			final var insertionEdit = relation.many()
										? buildListInsertionEdit(source, destParent, relation.name(), destIndexOpt.getAsInt(), formattedSubtree)
										: buildSingleInsertionEdit(source, destParent, formattedSubtree);
			editsByFile.computeIfAbsent(targetDocument.path(), ignored -> new ArrayList<>()).add(insertionEdit);

			return new MovePlanResult.Success(new MovePlannedEdit(copyEdits(editsByFile), true));
		}
		catch (MovePlanException | ReferenceEditPlanner.ReferenceEditException failure)
		{
			return new MovePlanResult.Failure(failure.getMessage());
		}
	}

	private static LinkNodeInternal<?, PNode, ?> resolveNode(final List<LinkNodeInternal<?, PNode, ?>> roots,
										  final String reference,
										  final String label)
	{
		final var resolution = new RootReferenceResolver().resolve(roots, reference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return found.node();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			throw new MovePlanException("Ambiguous " + label + ": " + reference + " (" + ambiguous.candidates().size() + " matches)");
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			throw new MovePlanException("Cannot resolve " + label + ": " + notFound.message());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			throw new MovePlanException("Cannot resolve " + label + ": " + failure.message());
		}

		throw new MovePlanException("Unexpected reference resolution state for " + label);
	}

	private static Relation<?, ?, ?, ?> resolveContainmentRelation(final LinkNodeInternal<?, PNode, ?> parentNode,
											  final String featureName)
	{
		final var group = parentNode.group();
		if (group == null)
		{
			return null;
		}

		return ModelUtil.streamAllFeatures(group)
						.filter(Relation.class::isInstance)
						.map(Relation.class::cast)
						.filter(Relation::contains)
						.filter(relation -> featureName.equals(relation.name()))
						.findFirst()
						.orElse(null);
	}

	private static boolean isCompatible(final Relation<?, ?, ?, ?> relation,
							 final LinkNodeInternal<?, PNode, ?> node)
	{
		final var concept = relation.concept();
		if (concept instanceof Group<?> group)
		{
			return ModelUtil.isSubGroup(group, node.group());
		}
		return false;
	}

	private static int indexOfChild(final LinkNodeInternal<?, PNode, ?> parent,
							   final String featureName,
							   final LinkNodeInternal<?, PNode, ?> child)
	{
		final var siblings = listChildren(parent, featureName);
		final int index = siblings.indexOf(child);
		if (index < 0)
		{
			throw new MovePlanException("Cannot resolve index of " + featureName + " child");
		}
		return index;
	}

	private static List<? extends LinkNodeInternal<?, PNode, ?>> listChildren(final LinkNodeInternal<?, PNode, ?> parent,
												  final String featureName)
	{
		return parent.streamChildren()
					.filter(child -> child.containingRelation() != null)
					.filter(child -> featureName.equals(child.containingRelation().name()))
					.toList();
	}

	private static Set<LinkNodeInternal<?, PNode, ?>> collectNodes(final LinkNodeInternal<?, PNode, ?> root)
	{
		final var nodes = new HashSet<LinkNodeInternal<?, PNode, ?>>();
		root.streamTree().forEach(nodes::add);
		return Set.copyOf(nodes);
	}

	private static Set<ObjectId> collectObjectIds(final Set<LinkNodeInternal<?, PNode, ?>> nodes)
	{
		final var ids = new HashSet<ObjectId>();
		for (final var node : nodes)
		{
			ids.add(resolveObjectId(node, "moved subtree"));
		}
		return Set.copyOf(ids);
	}

	private static ObjectId resolveObjectId(final LinkNodeInternal<?, PNode, ?> node, final String label)
	{
		try
		{
			final var built = node.build();
			final var id = ObjectId.from(built);
			if (id == null)
			{
				throw new MovePlanException("Cannot resolve " + label + " object id");
			}
			return id;
		}
		catch (RuntimeException e)
		{
			throw new MovePlanException("Cannot resolve " + label + " object id");
		}
	}

	private static String buildNewRootPath(final String parentPath,
								 final Relation<?, ?, ?, ?> relation,
								 final OptionalInt destIndexOpt,
								 final boolean sameList,
								 final OptionalInt sourceIndexOpt)
	{
		final var base = "/".equals(parentPath) ? "" : parentPath;
		final var featureName = relation.name();

		if (!relation.many())
		{
			return base + "/" + featureName;
		}

		if (destIndexOpt.isEmpty())
		{
			throw new MovePlanException("Missing destination index for list relation");
		}

		final int destIndex = destIndexOpt.getAsInt();
		final int finalIndex;

		if (sameList)
		{
			final int sourceIndex = sourceIndexOpt.getAsInt();
			finalIndex = destIndex > sourceIndex ? destIndex - 1 : destIndex;
		}
		else
		{
			finalIndex = destIndex;
		}

		return base + "/" + featureName + "." + finalIndex;
	}

	private static Map<ObjectId, String> buildMovedPathMap(final Set<ObjectId> movedIds,
									 final String oldRootPath,
									 final String newRootPath)
	{
		final var map = new HashMap<ObjectId, String>();
		for (final var id : movedIds)
		{
			final var updated = mapPath(id.path(), oldRootPath, newRootPath);
			if (updated != null)
			{
				map.put(id, updated);
			}
		}
		return Map.copyOf(map);
	}

	private static String mapPath(final String path, final String oldRootPath, final String newRootPath)
	{
		if (path == null || oldRootPath == null || newRootPath == null)
		{
			return null;
		}
		if (path.equals(oldRootPath))
		{
			return newRootPath;
		}
		final var prefix = oldRootPath.endsWith("/") ? oldRootPath : oldRootPath + "/";
		if (!path.startsWith(prefix))
		{
			return null;
		}
		final var suffix = path.substring(oldRootPath.length());
		if ("/".equals(newRootPath))
		{
			return suffix;
		}
		return newRootPath + suffix;
	}

	private static List<MoveIndexShift> buildShiftContexts(final String modelQualifiedName,
										  final String sourceParentPath,
										  final Relation<?, ?, ?, ?> sourceRelation,
										  final OptionalInt sourceIndexOpt,
										  final String destParentPath,
										  final Relation<?, ?, ?, ?> destRelation,
										  final OptionalInt destIndexOpt,
										  final boolean sameList)
	{
		final var shifts = new ArrayList<MoveIndexShift>();
		if (sameList)
		{
			shifts.add(MoveIndexShift.forReorder(modelQualifiedName,
										 sourceParentPath,
										 sourceRelation.name(),
										 sourceIndexOpt.getAsInt(),
										 destIndexOpt.getAsInt()));
			return List.copyOf(shifts);
		}

		if (sourceRelation.many() && sourceIndexOpt.isPresent())
		{
			shifts.add(MoveIndexShift.forRemoval(modelQualifiedName,
										 sourceParentPath,
										 sourceRelation.name(),
										 sourceIndexOpt.getAsInt()));
		}

		if (destRelation.many() && destIndexOpt.isPresent())
		{
			shifts.add(MoveIndexShift.forInsertion(modelQualifiedName,
										  destParentPath,
										  destRelation.name(),
										  destIndexOpt.getAsInt()));
		}

		return List.copyOf(shifts);
	}

	private static String rewriteReference(final String raw,
								 final ObjectId targetId,
								 final Map<ObjectId, String> movedPaths,
								 final List<MoveIndexShift> shifts)
	{
		if (raw == null || targetId == null)
		{
			return null;
		}

		final var movedPath = movedPaths.get(targetId);
		if (movedPath != null)
		{
			return rewriteMovedPath(raw, movedPath);
		}

		if (shifts != null)
		{
			for (final var shift : shifts)
			{
				final var updated = shift.rewriteRaw(targetId, raw);
				if (updated != null)
				{
					return updated;
				}
			}
		}

		return null;
	}

	private static String rewriteMovedPath(final String raw, final String newPath)
	{
		if (!ReferenceStringUtil.isPathLikeReference(raw))
		{
			return null;
		}

		if (raw.startsWith("#"))
		{
			final var modelToken = extractModelToken(raw);
			if (modelToken == null || modelToken.isBlank())
			{
				return null;
			}
			return "#" + modelToken + newPath;
		}

		return newPath;
	}

	private static String extractModelToken(final String raw)
	{
		if (raw == null || raw.startsWith("#") == false)
		{
			return null;
		}

		final int start = 1;
		if (raw.length() <= start)
		{
			return null;
		}

		final int slash = raw.indexOf('/', start);
		final int at = raw.indexOf('@', start);

		final int end = at >= 0 && (slash < 0 || at < slash)
						? at
						: slash >= 0 ? slash : raw.length();

		if (end <= start)
		{
			return null;
		}

		final var token = raw.substring(start, end);
		return token.isBlank() ? null : token;
	}

	private static String formatSubtree(final String source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(source, diagnostics);

		for (final var diagnostic : diagnostics)
		{
			if (diagnostic.severity() == LmDiagnostic.Severity.ERROR)
			{
				throw new MovePlanException("Moved subtree cannot be parsed: " + diagnostic.message());
			}
		}

		if (readResult.roots().size() != 1)
		{
			throw new MovePlanException("Moved subtree must contain exactly one root element; found: " + readResult.roots().size());
		}

		return new LmFormatter().format(readResult.roots());
	}

	private static TextEdits.TextEdit buildListInsertionEdit(final CharSequence source,
									   final LinkNodeInternal<?, PNode, ?> parentNode,
									   final String featureName,
									   final int insertionIndex,
									   final String formattedSubtree)
	{
		if (insertionIndex < 0)
		{
			throw new MovePlanException("Insertion index must be >= 0");
		}

		final var siblings = listChildren(parentNode, featureName);
		if (insertionIndex > siblings.size())
		{
			throw new MovePlanException("Insertion index out of range: " + insertionIndex + " (size=" + siblings.size() + ")");
		}

		if (insertionIndex == siblings.size())
		{
			return buildAppendChild(source, parentNode, formattedSubtree);
		}

		return buildInsertBeforeSibling(source, siblings.get(insertionIndex), formattedSubtree);
	}

	private static TextEdits.TextEdit buildSingleInsertionEdit(final CharSequence source,
										 final LinkNodeInternal<?, PNode, ?> parentNode,
										 final String formattedSubtree)
	{
		return buildAppendChild(source, parentNode, formattedSubtree);
	}

	private static TextEdits.TextEdit buildInsertBeforeSibling(final CharSequence source,
											   final LinkNodeInternal<?, PNode, ?> sibling,
											   final String formattedSubtree)
	{
		final var siblingSpan = locateSpan(source, sibling, "sibling subtree");
		final var baseIndent = trailingIndentBefore(source, siblingSpan.startOffset());
		final var indentedSubtree = indentSubtree(formattedSubtree, baseIndent);
		final var insertionText = indentedSubtree + "\n" + baseIndent;
		return new TextEdits.TextEdit(siblingSpan.startOffset(), 0, insertionText);
	}

	private static TextEdits.TextEdit buildAppendChild(final CharSequence source,
										   final LinkNodeInternal<?, PNode, ?> parentNode,
										   final String formattedSubtree)
	{
		final var parentSpan = locateSpan(source, parentNode, "parent subtree");
		final var baseIndentParent = trailingIndentBefore(source, parentSpan.startOffset());
		final var childIndent = baseIndentParent + INDENT;

		final int closingParenOffset = parentSpan.startOffset() + parentSpan.length() - 1;
		if (closingParenOffset < 0 || closingParenOffset >= source.length() || source.charAt(closingParenOffset) != ')')
		{
			throw new MovePlanException("Cannot locate parent closing token");
		}

		final int newlineBeforeClosing = lastNewline(source, parentSpan.startOffset(), closingParenOffset);
		final var indentedSubtree = indentSubtree(formattedSubtree, childIndent);

		if (newlineBeforeClosing < 0)
		{
			final var insertionText = "\n" + childIndent + indentedSubtree + "\n" + baseIndentParent;
			return new TextEdits.TextEdit(closingParenOffset, 0, insertionText);
		}

		final int insertionOffset = newlineBeforeClosing + 1;
		final var insertionText = childIndent + indentedSubtree + "\n";
		return new TextEdits.TextEdit(insertionOffset, 0, insertionText);
	}

	private static int lastNewline(final CharSequence source, final int start, final int endExclusive)
	{
		for (int i = Math.min(endExclusive - 1, source.length() - 1); i >= start && i >= 0; i--)
		{
			if (source.charAt(i) == '\n')
			{
				return i;
			}
		}
		return -1;
	}

	private static SubtreeSpanLocator.Span locateSpan(final CharSequence source,
										  final LinkNodeInternal<?, PNode, ?> node,
										  final String label)
	{
		final var span = SubtreeSpanLocator.locate(source, node);
		if (span == null)
		{
			throw new MovePlanException("Cannot locate " + label);
		}
		return span;
	}

	private static String indentSubtree(final String subtree, final String baseIndent)
	{
		if (baseIndent == null || baseIndent.isEmpty())
		{
			return subtree;
		}
		return subtree.replace("\n", "\n" + baseIndent);
	}

	private static String trailingIndentBefore(final CharSequence source, final int offset)
	{
		int start = offset;
		while (start > 0)
		{
			final char c = source.charAt(start - 1);
			if (c == ' ' || c == '\t')
			{
				start--;
				continue;
			}
			break;
		}
		return source.subSequence(start, offset).toString();
	}

	private static Map<Path, List<TextEdits.TextEdit>> copyEdits(final Map<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		final var out = new HashMap<Path, List<TextEdits.TextEdit>>();
		for (final var entry : editsByFile.entrySet())
		{
			out.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Map.copyOf(out);
	}
}
