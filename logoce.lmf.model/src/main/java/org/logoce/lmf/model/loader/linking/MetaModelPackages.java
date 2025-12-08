package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.DynamicModelPackage;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper for deriving the list of meta-model packages to use for linking,
 * based on the root header's {@code metamodels} property.
 * <p>
 * This logic was originally embedded in {@code LmLoader}; it is now
 * factored out so that tooling (e.g. an LSP) can reuse the same
 * meta-model selection behaviour.
 */
public final class MetaModelPackages
{
	/**
	 * System property controlling whether dynamic {@link IModelPackage} instances
	 * should be created when no generated {@code *Package} class can be resolved.
	 * <p>
	 * Property name: {@code org.logoce.lmf.model.dynamicModelPackage}.
	 * <ul>
	 *   <li>{@code true} (default) – fall back to {@link DynamicModelPackage}.</li>
	 *   <li>{@code false} – keep the previous behaviour and throw.</li>
	 * </ul>
	 */
	private static final boolean ENABLE_DYNAMIC_MODEL_PACKAGE =
		Boolean.parseBoolean(System.getProperty("org.logoce.lmf.model.dynamicModelPackage", "true"));

	private MetaModelPackages()
	{
	}

	/**
	 * Resolve meta-model packages for the given roots and registry.
	 * <p>
	 * Semantics:
	 * <ul>
	 *   <li>If there are no roots, returns LMCore only.</li>
	 *   <li>If the root has no {@code metamodels} header, returns LMCore only.</li>
	 *   <li>Otherwise, each entry is looked up as a model in the registry; if it is
	 *       a {@link MetaModel}, its generated {@link IModelPackage} is resolved and
	 *       added to the result.</li>
	 *   <li>If none of the configured metamodels can be resolved to packages,
	 *       falls back to LMCore only.</li>
	 * </ul>
	 */
	public static List<IModelPackage> resolve(final List<? extends Tree<PNode>> roots,
											  final ModelRegistry registry)
	{
		if (roots.isEmpty())
		{
			return List.of(LMCoreModelPackage.Instance);
		}

		final var rootNode = roots.getFirst().data();
		final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(rootNode);
		if (metamodelNames.isEmpty())
		{
			return List.of(LMCoreModelPackage.Instance);
		}

		final var packages = new ArrayList<IModelPackage>();
		for (final var name : metamodelNames)
		{
			final Model model = registry.getModel(name);
			if (model instanceof MetaModel metaModel)
			{
				packages.add(resolveModelPackage(metaModel));
			}
		}

		return packages.isEmpty() ? List.of(LMCoreModelPackage.Instance) : List.copyOf(packages);
	}

	public static IModelPackage resolveModelPackage(final MetaModel metaModel)
	{
		final StringBuilder pkg = new StringBuilder(metaModel.domain());

		try
		{
			return resolveGeneratedModelPackage(pkg, metaModel);
		}
		catch (Exception e)
		{
			if (ENABLE_DYNAMIC_MODEL_PACKAGE)
			{
				return new DynamicModelPackage(metaModel);
			}

			throw new IllegalStateException("Cannot resolve model package for metamodel " +
											metaModel.domain() +
											"." +
											metaModel.name(), e);
		}
	}

	private static IModelPackage resolveGeneratedModelPackage(final StringBuilder pkg, final MetaModel metaModel)
		throws Exception
	{
		final String extra = metaModel.extraPackage();
		if (extra != null && !extra.isBlank())
		{
			pkg.append('.').append(extra);
		}

		if (metaModel.genNamePackage())
		{
			pkg.append('.').append(metaModel.name().toLowerCase(Locale.ROOT));
		}

		final var basePackage = pkg.toString();
		final var className = basePackage + "." + metaModel.name() + "ModelPackage";

		final Class<?> clazz = Class.forName(className);
		if (!IModelPackage.class.isAssignableFrom(clazz))
		{
			throw new IllegalStateException("Class " + className + " does not implement IModelPackage");
		}

		@SuppressWarnings("unchecked")
		final Class<? extends IModelPackage> typed = (Class<? extends IModelPackage>) clazz;

		try
		{
			final var instanceField = typed.getField("Instance");
			return (IModelPackage) instanceField.get(null);
		}
		catch (NoSuchFieldException e)
		{
			return typed.getDeclaredConstructor().newInstance();
		}
	}
}
