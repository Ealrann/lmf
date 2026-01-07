package org.logoce.lmf.cli.rename;

public sealed interface RenamePlanResult permits RenamePlanResult.Success, RenamePlanResult.Failure
{
	record Success(RenamePlannedEdit edit) implements RenamePlanResult
	{
	}

	record Failure(String message) implements RenamePlanResult
	{
	}
}

