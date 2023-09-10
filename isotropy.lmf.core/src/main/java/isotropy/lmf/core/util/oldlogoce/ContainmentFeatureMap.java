package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.ArrayList;
import java.util.List;

public final class ContainmentFeatureMap
{
	private final List<ClassFeatureEntry> map = new ArrayList<>();

	public EReference[] features(final EClass eClass)
	{
		final var res = get(eClass);
		if (res == null)
		{
			return computeNew(eClass);
		}
		else
		{
			return res;
		}
	}

	private EReference[] get(final EClass eClass)
	{
		for (final var entry : map)
		{
			if (entry.eClass == eClass)
			{
				return entry.references;
			}
		}

		return null;
	}

	private EReference[] computeNew(final EClass eClass)
	{
		final var containmentFeatures = eClass.getEAllContainments().toArray(EReference[]::new);
		map.add(new ClassFeatureEntry(eClass, containmentFeatures));
		return containmentFeatures;
	}

	private record ClassFeatureEntry(EClass eClass, EReference[] references) {}
}
