package org.logoce.lmf.cli.assign;

import org.logoce.lmf.cli.format.RootReferenceResolver;

final class ReferenceResolutionMessage
{
	private ReferenceResolutionMessage()
	{
	}

	static String forResolution(final String reference,
								final RootReferenceResolver.Resolution resolution)
	{
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			return "Ambiguous reference: " + reference + " (" + ambiguous.candidates().size() + " matches)";
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			return "Cannot resolve reference: " + notFound.message();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			return "Cannot resolve reference: " + failure.message();
		}
		return "Cannot resolve reference: " + reference;
	}
}

