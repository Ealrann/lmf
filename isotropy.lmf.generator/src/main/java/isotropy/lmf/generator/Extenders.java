package isotropy.lmf.generator;

import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupResolution;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterProvider;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Extenders implements IAdapterProvider
{
	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(FeatureResolution.class, GroupResolution.class);
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}