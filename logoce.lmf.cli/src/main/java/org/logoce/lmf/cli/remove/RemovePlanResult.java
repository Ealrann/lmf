package org.logoce.lmf.cli.remove;

public sealed interface RemovePlanResult permits RemovePlanResult.Success, RemovePlanResult.Failure
{
	record Success(RemovePlannedEdit edit) implements RemovePlanResult
	{
	}

	record Failure(String message) implements RemovePlanResult
	{
	}
}
