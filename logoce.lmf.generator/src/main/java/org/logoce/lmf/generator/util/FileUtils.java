package org.logoce.lmf.generator.util;

import java.io.File;
import java.io.IOException;

public class FileUtils
{
	private static File getOrCreateChild(File folder, String name)
	{
		final var pkgFiles = folder.listFiles(f -> f.getName()
													.equals(name));
		if (pkgFiles != null && pkgFiles.length > 0)
		{
			return pkgFiles[0];
		}
		else
		{
			final var res = new File(folder, name);
			try
			{
				res.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return res;
		}
	}
}
