package org.logoce.lmf.cli.move;

public sealed interface MovePlanResult permits MovePlanResult.Success, MovePlanResult.Failure
{
	record Success(MovePlannedEdit edit) implements MovePlanResult
	{
	}

	record Failure(String message) implements MovePlanResult
	{
	}
}
