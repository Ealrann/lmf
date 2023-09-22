package logoce.lmf.generator;

import logoce.lmf.generator.adapter.*;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterProvider;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Extenders implements IAdapterProvider
{
	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(FeatureResolution.class,
					   GroupInterfaceType.class,
					   GroupImplementationType.class,
					   GroupBuilderInterfaceType.class,
					   GroupBuilderClassType.class,
					   ModelResolution.class);
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}