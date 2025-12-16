package org.logoce.lmf.core.util;

import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.Model;

import java.util.Optional;

/**
 * Helpers around resolving model references (e.g. {@code #Model@Type})
 * against a model header (domain, name, imports).
 * <p>
 * This mirrors the logic used by the linker {@code ImportResolver}, but exposes
 * a non-throwing API suitable for tooling and LSP integrations.
 */
public final class ModelImports
{
	private ModelImports()
	{
	}

	/**
	 * Resolve a model reference into a qualified model name using the owning {@link Model}'s
	 * header information (domain, name, imports).
	 * <p>
	 * This matches the resolution rules of {@code ImportResolver}:
	 * <ul>
	 *   <li>If {@code modelName} is null/blank: empty.</li>
	 *   <li>If {@code modelName} contains {@code '.'}: treated as fully qualified and returned as-is.</li>
	 *   <li>If it equals {@code "LMCore"}: LMCore's qualified name.</li>
	 *   <li>Otherwise, matched against the simple names of {@code owningModel.imports()}.</li>
	 *   <li>If still unresolved, matched against {@code owningModel.name()} (self-reference).</li>
	 * </ul>
	 *
	 * No registry lookups are performed here.
	 */
	public static Optional<String> resolveQualifiedName(final Model owningModel,
														final String modelName)
	{
		return resolveQualifiedName(owningModel.domain(), owningModel.name(), owningModel.imports(), modelName);
	}

	/**
	 * Resolve a model reference into a qualified model name using explicit header properties.
	 * This is the core algorithm shared with the linker.
	 */
	public static Optional<String> resolveQualifiedName(final String domain,
														final String name,
														final java.util.List<String> imports,
														final String modelName)
	{
		if (modelName == null || modelName.isEmpty())
		{
			return Optional.empty();
		}

		// Fully-qualified name: return as-is.
		if (modelName.contains("."))
		{
			return Optional.of(modelName);
		}

		// LMCore is implicitly available for all models.
		if (LMCoreModelPackage.MODEL.name().equals(modelName))
		{
			final var lmCore = LMCoreModelPackage.MODEL;
			return Optional.of(lmCore.domain() + "." + lmCore.name());
		}

		// Imports carry qualified names; compare their simple names.
		for (final String imp : imports)
		{
			final int lastDot = imp.lastIndexOf('.');
			final String simpleName = lastDot >= 0 ? imp.substring(lastDot + 1) : imp;
			if (simpleName.equals(modelName))
			{
				return Optional.of(imp);
			}
		}

		// Self-reference to the owning model.
		if (name.equals(modelName))
		{
			final String qualified = (domain == null || domain.isBlank())
									 ? name
									 : domain + "." + name;
			return Optional.of(qualified);
		}

		return Optional.empty();
	}

	/**
	 * Resolve a model reference into a model instance using the owning {@link Model}'s header
	 * plus a {@link ModelRegistry} for existence checks and optional fallback by simple name.
	 *
	 * @param owningModel the model that owns the reference
	 * @param modelName   the referenced model name (simple or qualified)
	 * @param registry    registry to look up models
	 * @return the resolved model, or {@code Optional.empty()} if it cannot be resolved
	 */
	public static Optional<Model> resolveModel(final Model owningModel,
											   final String modelName,
											   final ModelRegistry registry)
	{
		final var qualifiedFromHeader = resolveQualifiedName(owningModel, modelName);
		if (qualifiedFromHeader.isPresent())
		{
			final var model = registry.getModel(qualifiedFromHeader.get());
			if (model != null)
			{
				return Optional.of(model);
			}
		}

		// Fallback: first model in the registry with matching simple name.
		for (final Model candidate : (Iterable<Model>) registry.models()::iterator)
		{
			if (candidate.name().equals(modelName))
			{
				return Optional.of(candidate);
			}
		}

		return Optional.empty();
	}
}
