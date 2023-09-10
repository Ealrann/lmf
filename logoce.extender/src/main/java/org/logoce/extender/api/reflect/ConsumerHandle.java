package org.logoce.extender.api.reflect;

public interface ConsumerHandle extends ExecutionHandle
{
	void invoke(Object... parameters);

	Object getLambdaFunction();
}
