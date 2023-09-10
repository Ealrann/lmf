package org.logoce.extender.api.reflect;

public interface SupplierHandle extends ExecutionHandle
{
	Object invoke();

	Object getLambdaFunction();
}
