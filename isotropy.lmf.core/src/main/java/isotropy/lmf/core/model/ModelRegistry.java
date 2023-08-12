package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.LMCorePackage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class ModelRegistry
{
	public static final ModelRegistry Instance = new ModelRegistry();

	private final Map<String, IModelPackage> modelMap = new HashMap<>();

	private ModelRegistry()
	{
		final var coreModelPackage = LMCorePackage.Instance;
		modelMap.put(coreModelPackage.model().name(), coreModelPackage);
	}

	public IModelPackage get(final String name)
	{
		return modelMap.get(name);
	}

	public Stream<IModelPackage> models()
	{
		return modelMap.values().stream();
	}

	public void register(final IModelPackage modelPackage)
	{
		modelMap.put(modelPackage.model().name(), modelPackage);
	}
}
