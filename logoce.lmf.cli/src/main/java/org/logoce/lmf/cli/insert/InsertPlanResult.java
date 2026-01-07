package org.logoce.lmf.cli.insert;

public sealed interface InsertPlanResult permits InsertPlanResult.Success, InsertPlanResult.Failure
{
	record Success(InsertPlannedEdit edit) implements InsertPlanResult
	{
	}

	record Failure(String message) implements InsertPlanResult
	{
	}
}

