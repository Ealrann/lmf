package org.logoce.lmf.model.util;

public final class DebugUtil
{
	private static final String DEBUG_ARG = "debug";
	private static final String DEBUG_VERBOSE_ARG = "debugVerbose";
	private static final String DEBUG_ALLOCATION_ARG = "debugAllocation";

	public static boolean DEBUG_ENABLED;
	public static boolean DEBUG_VERBOSE_ENABLED;
	public static boolean DEBUG_ALLOCATION;

	static
	{
		final String debugVerboseProperty = System.getProperty(DEBUG_VERBOSE_ARG);
		final String debugAllocation = System.getProperty(DEBUG_ALLOCATION_ARG);
		final String debugProperty = System.getProperty(DEBUG_ARG);
		DEBUG_ALLOCATION = (debugAllocation != null && debugAllocation.equals("false") == false);
		DEBUG_VERBOSE_ENABLED = debugVerboseProperty != null && debugVerboseProperty.equals("false") == false;
		DEBUG_ENABLED = (debugProperty != null && debugProperty.equals("false") == false) || DEBUG_VERBOSE_ENABLED || DEBUG_ALLOCATION;
	}

	public static void parseMainArgs(String[] args)
	{
		for (var arg : args)
		{
			if (DEBUG_ARG.equals(arg))
			{
				DebugUtil.DEBUG_ENABLED = true;
			}
			if (DEBUG_VERBOSE_ARG.equals(arg))
			{
				DebugUtil.DEBUG_ENABLED = true;
				DebugUtil.DEBUG_VERBOSE_ENABLED = true;
			}
			if (DEBUG_ALLOCATION_ARG.equals(arg))
			{
				DebugUtil.DEBUG_ENABLED = true;
				DebugUtil.DEBUG_ALLOCATION = true;
			}
		}
	}

	private DebugUtil()
	{
	}
}
