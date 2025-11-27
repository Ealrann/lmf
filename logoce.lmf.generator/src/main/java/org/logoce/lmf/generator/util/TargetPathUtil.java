package org.logoce.lmf.generator.util;

import org.logoce.lmf.model.lang.MetaModel;

import java.io.File;
import java.util.Locale;

public final class TargetPathUtil
{
	private TargetPathUtil()
	{
	}

	public static File resolve(final File baseDir, final MetaModel model)
	{
		final var domainPath = model.domain().replace('.', File.separatorChar);
		var target = baseDir.toPath().toString().endsWith(domainPath)
						? baseDir
						: new File(baseDir, domainPath);

		final var extraPackage = model.extraPackage();
		if (extraPackage != null && !extraPackage.isBlank())
		{
			target = new File(target, extraPackage);
		}

		if (model.genNamePackage())
		{
			target = new File(target, model.name().toLowerCase(Locale.ROOT));
		}

		return target;
	}
}
