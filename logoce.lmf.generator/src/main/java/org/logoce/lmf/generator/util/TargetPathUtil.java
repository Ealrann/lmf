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
		var target = new File(baseDir, model.domain().replace('.', File.separatorChar));

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

	public static String packageName(final MetaModel model)
	{
		final var base = new StringBuilder(model.domain());

		final var extraPackage = model.extraPackage();
		if (extraPackage != null && !extraPackage.isBlank())
		{
			base.append('.').append(extraPackage);
		}

		if (model.genNamePackage())
		{
			base.append('.').append(model.name().toLowerCase(Locale.ROOT));
		}

		return base.toString();
	}
}
