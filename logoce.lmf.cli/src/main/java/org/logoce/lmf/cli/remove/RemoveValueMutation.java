package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;

enum RemoveMutationKind
{
	REMOVE,
	SHIFT
}

record RemoveValueMutation(RemoveMutationKind kind,
						   FeatureValueSpanIndex.ValueSpan valueSpan,
						   String newRaw)
{
}
