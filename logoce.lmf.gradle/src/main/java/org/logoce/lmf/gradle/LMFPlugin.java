package org.logoce.lmf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LMFPlugin implements Plugin<Project>
{
	@Override
	public void apply(final Project project)
	{
		final var extension = project.getExtensions().create("lmf", LMFExtension.class);

		extension.getIncludes().convention(List.of("**/*.lm"));
		extension.getOutputDir().convention(project.getLayout().getBuildDirectory().dir("generated/sources/lmf"));
		configureDefaultImportModels(project, extension);

		final var generationLock = project.getGradle()
										  .getSharedServices()
										  .registerIfAbsent("lmfGenerationLock",
															LmfGenerationLockService.class,
															spec -> spec.getMaxParallelUsages().set(1));

		project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
			final var javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
			javaExtension.getSourceSets().all(sourceSet -> {
				final var sourceSetName = sourceSet.getName();
				final var capitalized = Character.toUpperCase(sourceSetName.charAt(0)) + sourceSetName.substring(1);
				final var taskName = "generateLmf" + capitalized;

				final var baseDir = project.getLayout()
										   .getProjectDirectory()
										   .dir("src/" + sourceSetName)
										   .getAsFile();

				final var outputDir = extension.getOutputDir().dir(sourceSetName);

				final var generateTask = project.getTasks()
												.register(taskName, GenerateLmfSources.class, task -> {
													task.setGroup("LMF");
														task.setDescription(
																"Generates Java sources from LMF model files for source set "
																+ sourceSetName);

														task.getOutputDir().set(outputDir);
														task.getImportModelFiles().from(extension.getImportModels());
														task.usesService(generationLock);

													task.getModelFiles()
														.from(project.fileTree(baseDir, spec -> {
															final var includes = extension.getIncludes().get();
															spec.include(includes);
															}));
												});

				sourceSet.getJava().srcDir(outputDir);

				project.getTasks()
					   .named(sourceSet.getCompileJavaTaskName())
					   .configure(task -> task.dependsOn(generateTask));
			});
		});
	}

	private static void configureDefaultImportModels(final Project project, final LMFExtension extension)
	{
		final var importModels = extension.getImportModels();

		importModels.from(findModelFiles(project, project.getRootProject().getProjectDir()));

		for (final var includedBuild : collectIncludedBuilds(project.getGradle()))
		{
			// The LMF runtime already provides LMCore via ModelRegistry.empty(); importing LMF's own models is usually
			// unnecessary and can accidentally pull non-metamodel `.lm` test assets into generation.
			if ("lmf".equalsIgnoreCase(includedBuild.getProjectDir().getName())) continue;
			importModels.from(findModelFiles(project, includedBuild.getProjectDir()));
		}
	}

	private static Set<IncludedBuild> collectIncludedBuilds(final Gradle gradle)
	{
		final Set<IncludedBuild> result = new LinkedHashSet<>();
		Gradle current = gradle;
		while (current != null)
		{
			result.addAll(current.getIncludedBuilds());
			current = current.getParent();
		}
		return result;
	}

	private static FileTree findModelFiles(final Project project, final File baseDir)
	{
		return project.fileTree(baseDir, spec -> {
			// Only M2 MetaModels are expected here; avoid picking up M1 instance `.lm` files from tests.
			spec.include("**/src/main/model/**/*.lm");
			spec.exclude("**/src/*/generated/**");
			spec.exclude("**/build/**");
			spec.exclude("**/.gradle/**");
			spec.exclude("**/LMCore.lm");
		});
	}
}
