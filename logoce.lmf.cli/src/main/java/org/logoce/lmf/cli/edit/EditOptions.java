package org.logoce.lmf.cli.edit;

public record EditOptions(boolean format, boolean validate, boolean force, boolean commit)
{
	public static EditOptions defaults()
	{
		return new EditOptions(true, true, false, true);
	}
}

