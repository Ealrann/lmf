package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

final class RemoveTargetResolver
{
	LinkNodeInternal<?, PNode, ?> resolve(final LmDocument document, final String targetReference)
	{
		if (document == null)
		{
			throw new RemovePlanException("Target document is missing");
		}

		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			throw new RemovePlanException("Target document has no link trees");
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return found.node();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			throw new RemovePlanException("Ambiguous reference: " + targetReference + " (" + ambiguous.candidates().size() + " matches)");
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			throw new RemovePlanException("Cannot resolve reference: " + notFound.message());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			throw new RemovePlanException("Cannot resolve reference: " + failure.message());
		}

		throw new RemovePlanException("Unexpected reference resolution state");
	}
}
