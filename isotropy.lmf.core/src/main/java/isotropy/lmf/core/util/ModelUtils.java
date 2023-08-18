package isotropy.lmf.core.util;

import isotropy.lmf.core.lang.Group;

public class ModelUtils
{
	public static boolean isSubGroup(final Group<?> parent, final Group<?> check)
	{
		if (check == parent)
		{
			return true;
		}
		else if (check.includes()
					  .isEmpty() == false)
		{
			for (final var include : check.includes())
			{
				if (isSubGroup(parent, include.group()))
				{
					return true;
				}
			}
		}
		return false;
	}}
