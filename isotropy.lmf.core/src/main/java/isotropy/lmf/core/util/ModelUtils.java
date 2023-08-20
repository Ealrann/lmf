package isotropy.lmf.core.util;

import isotropy.lmf.core.lang.Concept;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Group;

public class ModelUtils
{
	public static boolean isSubGroup(final Concept<?> parent, final Group<?> check)
	{

		if (parent instanceof Group<?> parentGroup)
		{
			return isSubGroup(parentGroup, check);
		}
		else if (parent instanceof Generic<?> genericParent && genericParent.type() instanceof Group<?> parentGroup)
		{
			return isSubGroup(parentGroup, check);
		}
		else
		{
			return false;
		}
	}

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
				if (isSubGroup(parent, (Group<?>) include.group()))
				{
					return true;
				}
			}
		}
		return false;
	}
}
