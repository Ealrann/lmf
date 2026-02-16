package org.logoce.lmf.cli.batch;

public record BatchOperationResult(int exitCode, boolean validationFailed, BatchOperationReport report)
{
}
