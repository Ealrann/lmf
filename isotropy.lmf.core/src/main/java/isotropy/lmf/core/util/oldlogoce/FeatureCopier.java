package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

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

	@SuppressWarnings("unchecked")
	public void copyFeatures(final EObject src, final EObject trg, final List<EStructuralFeature> features)
	{
		for (final var feature : features)
		{
			final boolean isContainment = feature instanceof EReference reference && reference.isContainment();
			if (isContainment)
			{
				if (feature.isMany() == false)
				{
					final var srcValue = (EObject) src.eGet(feature);
					final var trgValue = EcoreUtil.copy(srcValue);
					trg.eSet(feature, trgValue);
				}
				else
				{
					final var srcList = (EList<EObject>) src.eGet(feature);
					final var trgList = (EList<EObject>) trg.eGet(feature);
					for (final EObject srcValue : srcList)
					{
						final var trgValue = EcoreUtil.copy(srcValue);
						trgList.add(trgValue);
					}
				}
			}
		}
		for (final var feature : features)
		{
			final boolean isContainment = feature instanceof EReference reference && reference.isContainment();
			if (isContainment == false)
			{
				if (feature.isMany() == false)
				{
					final var srcValue = src.eGet(feature);
					final var trgValue = resolve(src, trg, srcValue);
					trg.eSet(feature, trgValue);
				}
				else
				{
					final var listSrc = (EList<Object>) src.eGet(feature);
					final var listTrg = (EList<Object>) trg.eGet(feature);

					for (final var srcValue : listSrc)
					{
						final var trgValue = resolve(src, trg, srcValue);
						listTrg.add(trgValue);
					}
				}
			}
		}
	}

	private Object resolve(final EObject src, final EObject trg, final Object srcValue)
	{
		if (resolve && srcValue instanceof EObject srcEOValue)
		{
			return ModelUtil.containmentPath(src, srcEOValue).map(path -> path.eGet(trg)).orElse(srcEOValue);
		}
		else
		{
			return srcValue;
		}
	}
}
