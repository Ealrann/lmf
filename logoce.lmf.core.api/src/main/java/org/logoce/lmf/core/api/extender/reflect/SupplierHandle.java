package org.logoce.lmf.core.api.extender.reflect;

public interface SupplierHandle extends ExecutionHandle
{
	Object invoke();

	Object getLambdaFunction();
}
