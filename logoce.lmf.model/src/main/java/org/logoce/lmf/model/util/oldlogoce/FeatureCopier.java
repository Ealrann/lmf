package org.logoce.lmf.model.util.oldlogoce;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;

public final class FeatureCopier
{
	private final boolean resolve;

	public FeatureCopier()
	{
		this(false);
	}

	public FeatureCopier(final boolean resolve)
	{
		this.resolve = resolve;
	}
/*
	@SuppressWarnings("unchecked")
	public void copyFeatures(final LMObject src, final LMObject trg, final List<RawFeature<?, ?>> features)
	{
		for (final var feature : features)
		{
			if (feature.featureSupplier().get() instanceof Relation<?, ?> reference &&
				reference.contains())
			{
				if (feature.many() == false)
				{
					final var srcValue = (LMObject) src.get(reference);
					final var trgValue = EcoreUtil.copy(srcValue);
					trg.set(feature, trgValue);
				}
				else
				{
					final var srcList = (List<LMObject>) src.get(feature);
					final var trgList = (List<LMObject>) trg.get(feature);
					for (final LMObject srcValue : srcList)
					{
						final var trgValue = EcoreUtil.copy(srcValue);
						trgList.add(trgValue);
					}
				}
			}
		}
		for (final var feature : features)
		{
			final boolean isContainment = feature.featureSupplier().get() instanceof Relation<?,?> reference && reference.contains();
			if (isContainment == false)
			{
				if (feature.many() == false)
				{
					final var srcValue = src.get(feature);
					final var trgValue = resolve(src, trg, srcValue);
					trg.set(feature, trgValue);
				}
				else
				{
					final var listSrc = (List<Object>) src.get(feature);
					final var listTrg = (List<Object>) trg.get(feature);

					for (final var srcValue : listSrc)
					{
						final var trgValue = resolve(src, trg, srcValue);
						listTrg.add(trgValue);
					}
				}
			}
		}
	}

	private Object resolve(final LMObject src, final LMObject trg, final Object srcValue)
	{
		if (resolve && srcValue instanceof LMObject srcEOValue)
		{
			return ModelUtils.containmentPath(src, srcEOValue).map(path -> path.eGet(trg)).orElse(srcEOValue);
		}
		else
		{
			return srcValue;
		}
	}*/
}
