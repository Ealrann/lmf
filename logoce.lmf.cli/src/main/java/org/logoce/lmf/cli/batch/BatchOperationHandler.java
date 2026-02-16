package org.logoce.lmf.cli.batch;

interface BatchOperationHandler
{
	BatchOperationResult execute(BatchOperation operation);
}
