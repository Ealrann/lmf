package org.logoce.lmf.core.loader.api.loader;

import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LmModelLinker;
import org.logoce.lmf.core.loader.api.loader.linking.MetaModelPackages;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.util.tree.Tree;

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
		return loadModel(readResult, diagnostics);
	}

	public LmDocument loadModel(final LmTreeReader.ReadResult readResult, final List<LmDiagnostic> diagnostics)
	{
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

		final var model = linkResult.model();
		if (model != null)
		{
			final var qualifiedName = qualifiedName(model);
			if (qualifiedName != null &&
				!qualifiedName.equals(qualifiedName(LMCoreModelPackage.MODEL)) &&
				effectiveRegistry.getModel(qualifiedName) != null)
			{
				diagnostics.add(new LmDiagnostic(1,
												1,
												1,
												0,
												LmDiagnostic.Severity.ERROR,
												"Model already exists in registry: " + qualifiedName));
				return new LmDocument(null,
									  List.copyOf(diagnostics),
									  roots,
									  readResult.source(),
									  linkResult.trees());
			}
		}

		return new LmDocument(model,
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
		final var effectiveBase = ensureLmCore(baseRegistry);
		if (documents == null || documents.isEmpty())
		{
			return effectiveBase;
		}

		final var lmCoreQualifiedName = qualifiedName(LMCoreModelPackage.MODEL);
		final var parsedModels = new ArrayList<ParsedModel>();
		for (final LmDocument doc : documents)
		{
			for (final Tree<PNode> root : doc.roots())
			{
				final var parsed = ParsedModel.from(root, doc.source());
				if (lmCoreQualifiedName != null && lmCoreQualifiedName.equals(parsed.qualifiedName()))
				{
					continue;
				}
				parsedModels.add(parsed);
			}
		}

		if (parsedModels.isEmpty())
		{
			return effectiveBase;
		}

		final var builderBase = new ModelRegistry.Builder(effectiveBase);
		for (final var pm : parsedModels)
		{
			builderBase.remove(pm.qualifiedName());
		}
		final var linkBase = ensureLmCore(builderBase.build());

		final List<Model> models = MultiModelSupport.buildAll(parsedModels, linkBase);

		final var builder = new ModelRegistry.Builder(linkBase);
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

		final var duplicate = linkResult.roots()
										.stream()
										.filter(Model.class::isInstance)
										.map(Model.class::cast)
										.map(LmLoader::qualifiedName)
										.filter(q -> q != null &&
													 !q.equals(qualifiedName(LMCoreModelPackage.MODEL)) &&
													 modelRegistry.getModel(q) != null)
										.findFirst()
										.orElse(null);
		if (duplicate != null)
		{
			throw new IllegalStateException("Model already exists in registry: " + duplicate);
		}

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
			if (parsedModels.isEmpty())
			{
				return List.of();
			}

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
					if (!allImportsAvailable(pm.imports(), availableNames))
					{
						continue;
					}

					final var linker = new LmModelLinker<PNode>(registryBuilder.build());
					final var diagnostics = new ArrayList<LmDiagnostic>();
					final var linkResult = linker.linkModel(List.of(pm.tree()), diagnostics, pm.source());

					final var model = linkResult.model();
					if (model == null)
					{
						throw new IllegalArgumentException("Input doesn't define a valid model: " +
														   pm.qualifiedName() +
														   formatDiagnostics(diagnostics));
					}

					registryBuilder.register(model);
					availableNames.add(domainName(model));
					builtByName.put(pm.qualifiedName(), model);
					it.remove();
					progressed = true;
				}

				if (!progressed)
				{
					final var unresolved = new StringBuilder();
					for (final var pm : remaining)
					{
						if (unresolved.length() != 0)
						{
							unresolved.append(", ");
						}
						unresolved.append(pm.qualifiedName());
					}
					throw new IllegalStateException("Cannot resolve all imports between provided models: " +
													unresolved);
				}
			}

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

		private static String formatDiagnostics(final List<LmDiagnostic> diagnostics)
		{
			if (diagnostics == null || diagnostics.isEmpty())
			{
				return "";
			}

			final int maxErrors = 5;
			final var sb = new StringBuilder();
			int count = 0;

			for (final var diagnostic : diagnostics)
			{
				if (diagnostic.severity() != LmDiagnostic.Severity.ERROR)
				{
					continue;
				}
				if (count == maxErrors)
				{
					sb.append("; ...");
					break;
				}
				if (count > 0)
				{
					sb.append("; ");
				}
				sb.append(diagnostic.line())
				  .append(':')
				  .append(diagnostic.column())
				  .append(' ')
				  .append(diagnostic.message());
				count++;
			}

			if (sb.length() == 0)
			{
				return "";
			}
			return " (errors: " + sb + ")";
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
			return qualifiedName(model);
		}
	}

	private static String qualifiedName(final Model model)
	{
		if (model == null) return null;

		final var domain = model.domain();
		final var name = model.name();
		if (domain == null || domain.isBlank())
		{
			return name;
		}
		return domain + "." + name;
	}
}
