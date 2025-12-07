package org.logoce.lmf.model.loader;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.linking.MetaModelPackages;
import org.logoce.lmf.model.loader.model.LmDocument;
import org.logoce.lmf.model.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * High-level loader for .lm models, designed for tooling integration.
 * <p>
 * It exposes both the parsed tree ({@link PNode}) and the linked {@link Model}
 * along with structured diagnostics and the original source text.
 */
public final class LmLoader
{
	private final ModelRegistry modelRegistry;

	public LmLoader(final ModelRegistry modelRegistry)
	{
		this.modelRegistry = modelRegistry;
	}

	public static LmLoader withEmptyRegistry()
	{
		return new LmLoader(ModelRegistry.empty());
	}

	public LmDocument loadModel(final InputStream inputStream) throws IOException
	{
		final var source = readAll(inputStream);
		return loadModel(source);
	}

	public LmDocument loadModel(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var readResult = treeReader.read(source, diagnostics);

		final List<Tree<PNode>> roots = readResult.roots();
		if (roots.isEmpty())
		{
			return new LmDocument(null,
								  List.copyOf(diagnostics),
								  roots,
								  readResult.source(),
								  List.of());
		}

		final var isMetaModelRoot = ModelHeaderUtil.isMetaModelRoot(roots);
		final var effectiveRegistry = isMetaModelRoot
									  ? ensureLmCore(modelRegistry)
									  : modelRegistry;

		final var metaPackages = isMetaModelRoot
								 ? List.<IModelPackage>of(LMCoreModelPackage.Instance)
								 : MetaModelPackages.resolve(roots, effectiveRegistry);

		final var linker = new LmModelLinker<PNode>(effectiveRegistry, metaPackages);
		final var linkResult = linker.linkModel(roots, diagnostics, readResult.source());

		if (!isMetaModelRoot && !ModelHeaderUtil.resolveMetamodelNames(roots.getFirst().data()).isEmpty())
		{
			diagnostics.removeIf(d -> d.severity() == LmDiagnostic.Severity.ERROR &&
									  "Root element is not a Model; use loadObject() for generic roots".equals(
										  d.message()));
		}

		return new LmDocument(linkResult.model(),
							  List.copyOf(diagnostics),
							  roots,
							  readResult.source(),
							  linkResult.trees());
	}

	/**
	 * Build a {@link ModelRegistry} from multiple {@link LmDocument}s, reusing the
	 * internal multi-model import resolution logic. All models from {@code baseRegistry}
	 * are visible as imports while linking.
	 */
	public static ModelRegistry buildRegistry(final List<LmDocument> documents, final ModelRegistry baseRegistry)
	{
		if (documents == null || documents.isEmpty())
		{
			return baseRegistry;
		}

		final var parsedModels = new ArrayList<ParsedModel>();
		for (final LmDocument doc : documents)
		{
			for (final Tree<PNode> root : doc.roots())
			{
				parsedModels.add(ParsedModel.from(root, doc.source()));
			}
		}

		final List<Model> models = MultiModelSupport.buildAll(parsedModels, baseRegistry);

		final var builder = new ModelRegistry.Builder(baseRegistry);
		for (final Model model : models)
		{
			builder.register(model);
		}
		return builder.build();
	}

	/**
	 * Load arbitrary LMObjects from a single source (not just a Model root).
	 * Mirrors the legacy {@code ResourceUtil.loadObject} behavior but uses the new loader/linker stack.
	 */
	public List<? extends LMObject> loadObjects(final InputStream inputStream) throws IOException
	{
		final var source = readAll(inputStream);
		return loadObjects(source);
	}

	public List<? extends LMObject> loadObjects(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var readResult = treeReader.read(source, diagnostics);

		final var roots = readResult.roots();
		if (roots.isEmpty())
		{
			return List.of();
		}

		final var metaPackages = ModelHeaderUtil.isMetaModelRoot(roots)
								 ? List.<IModelPackage>of(LMCoreModelPackage.Instance)
								 : MetaModelPackages.resolve(roots, modelRegistry);

		final var linker = new LmModelLinker<PNode>(modelRegistry, metaPackages);
		final var linkResult = linker.linkModelStrict(roots);
		return linkResult.roots();
	}

	/**
	 * Load multiple models with import resolution, similar to {@code ResourceUtil.loadModels}.
	 * <p>
	 * Each root {@code (MetaModel ...)} in the provided streams is treated as a separate model.
	 */
	public List<Model> loadModels(final List<InputStream> inputStreams) throws IOException
	{
		final var treeReader = new LmTreeReader();
		final var parsedModels = new ArrayList<ParsedModel>();

		for (final var in : inputStreams)
		{
			final var source = readAll(in);
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = treeReader.read(source, diagnostics);

			if (readResult.roots().isEmpty())
			{
				if (diagnostics.stream().anyMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR))
				{
					final var first = diagnostics.getFirst();
					throw new IllegalArgumentException("Failed to parse model at " +
													   first.line() +
													   ":" +
													   first.column() +
													   " - " +
													   first.message());
				}
				continue;
			}

			for (final var root : readResult.roots())
			{
				parsedModels.add(ParsedModel.from(root, readResult.source()));
			}
		}

		return MultiModelSupport.buildAll(parsedModels, modelRegistry);
	}

	private static ModelRegistry ensureLmCore(final ModelRegistry registry)
	{
		final var existing = registry.getModel(LMCoreModelPackage.MODEL.domain(), LMCoreModelPackage.MODEL.name());
		if (existing != null)
		{
			return registry;
		}

		final var builder = new ModelRegistry.Builder(registry);
		builder.register(LMCoreModelPackage.MODEL);
		return builder.build();
	}


	private static String readAll(final InputStream in) throws IOException
	{
		try (final var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
		{
			final var sb = new StringBuilder();
			String line;
			boolean first = true;
			while ((line = reader.readLine()) != null)
			{
				if (!first)
				{
					sb.append('\n');
				}
				sb.append(line);
				first = false;
			}
			return sb.toString();
		}
	}

	private record ParsedModel(Tree<PNode> tree, CharSequence source, String qualifiedName, List<String> imports)
	{
		static ParsedModel from(final Tree<PNode> tree, final CharSequence source)
		{
			final var node = tree.data();
			final var imports = ModelHeaderUtil.resolveImports(node);
			final var domain = ModelHeaderUtil.resolveDomain(node);
			final var name = ModelHeaderUtil.resolveName(node);
			final var qualifiedName = domain == null || domain.isBlank() ? name : domain + "." + name;
			return new ParsedModel(tree, source, qualifiedName, imports);
		}
	}

	private static final class MultiModelSupport
	{
		private MultiModelSupport()
		{
		}

		static List<Model> buildAll(final List<ParsedModel> parsedModels, final ModelRegistry baseRegistry)
		{
			if (parsedModels.isEmpty()) return List.of();

			final var remaining = new ArrayList<>(parsedModels);
			final var builtByName = new java.util.HashMap<String, Model>();

			final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
			final var availableNames = new HashSet<String>();
			baseRegistry.models().forEach(model -> availableNames.add(domainName(model)));

			while (!remaining.isEmpty())
			{
				boolean progressed = false;
				final var it = remaining.iterator();
				while (it.hasNext())
				{
					final var pm = it.next();
					if (allImportsAvailable(pm.imports(), availableNames))
					{
						final var linker = new LmModelLinker<PNode>(registryBuilder.build());
						final var diagnostics = new ArrayList<LmDiagnostic>();
						final var linkResult = linker.linkModel(List.of(pm.tree()), diagnostics, pm.source());

						final var model = linkResult.model();
						if (model == null)
						{
							throw new IllegalArgumentException("Input doesn't define a valid model: " +
															   pm.qualifiedName());
						}

						registryBuilder.register(model);
						availableNames.add(domainName(model));
						builtByName.put(pm.qualifiedName(), model);
						it.remove();
						progressed = true;
					}
				}

				if (!progressed)
				{
					final var unresolved = new StringBuilder();
					for (final var pm : remaining)
					{
						if (!unresolved.isEmpty())
						{
							unresolved.append(", ");
						}
						unresolved.append(pm.qualifiedName());
					}
					throw new IllegalStateException("Cannot resolve all imports between provided models: " +
													unresolved);
				}
			}

			// Preserve the original parsedModels order in the returned list, as the legacy
			// MultiModelLoader did via its BuildingModel list.
			final var ordered = new ArrayList<Model>(parsedModels.size());
			for (final var pm : parsedModels)
			{
				final var model = builtByName.get(pm.qualifiedName());
				if (model == null)
				{
					throw new IllegalStateException("No built model for " + pm.qualifiedName());
				}
				ordered.add(model);
			}
			return List.copyOf(ordered);
		}

		private static boolean allImportsAvailable(final List<String> imports, final Set<String> available)
		{
			for (final var imp : imports)
			{
				if (!available.contains(imp))
				{
					return false;
				}
			}
			return true;
		}

		private static String domainName(final Model model)
		{
			if (model instanceof MetaModel mm)
			{
				return mm.domain() + "." + mm.name();
			}
			else
			{
				return model.name();
			}
		}
	}
}
