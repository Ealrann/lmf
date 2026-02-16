package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BatchOperationExecutor
{
	private final BatchExecutionContext context;
	private final BatchOperationSupport support;
	private final Map<String, BatchOperationHandler> handlers;

	public BatchOperationExecutor(final BatchExecutionContext context)
	{
		this.context = Objects.requireNonNull(context, "context");
		this.support = new BatchOperationSupport(context);
		this.handlers = Map.of("rename", new BatchRenameHandler(support),
							  "remove", new BatchRemoveHandler(support),
							  "insert", new BatchInsertHandler(support),
							  "move", new BatchMoveHandler(support),
							  "set", new BatchAssignmentHandler(support, BatchAssignmentHandler.setSpec()),
							  "unset", new BatchAssignmentHandler(support, BatchAssignmentHandler.unsetSpec()),
							  "add", new BatchAssignmentHandler(support, BatchAssignmentHandler.addSpec()),
							  "remove-value", new BatchAssignmentHandler(support, BatchAssignmentHandler.removeValueSpec()),
							  "clear", new BatchAssignmentHandler(support, BatchAssignmentHandler.clearSpec()),
							  "replace", new BatchReplaceHandler(support));
	}

	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var handler = handlers.get(operation.command());
		if (handler != null)
		{
			return handler.execute(operation);
		}

		context.err().println(support.opPrefix(operation) + "Unknown command: " + operation.command());
		context.err().println(support.opPrefix(operation) + "Raw: " + operation.rawLine());
		return support.result(operation,
						  ExitCodes.USAGE,
						  false,
						  "error",
						  "Unknown command: " + operation.command(),
						  List.of(),
						  List.of());
	}
}
