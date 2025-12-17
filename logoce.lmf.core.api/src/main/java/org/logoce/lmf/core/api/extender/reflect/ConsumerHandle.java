package org.logoce.lmf.core.api.extender.reflect;

public interface ConsumerHandle extends ExecutionHandle
{
	void invoke(Object... parameters);

	Object getLambdaFunction();
}
