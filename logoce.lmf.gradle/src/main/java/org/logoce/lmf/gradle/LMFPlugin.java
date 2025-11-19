package org.logoce.lmf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

import java.util.List;

public final class LMFPlugin implements Plugin<Project>
{
	@Override
	public void apply(final Project project)
	{
		final var extension = project.getExtensions().create("lmf", LMFExtension.class);

		extension.getIncludes().convention(List.of("**/*.lm"));

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

				final var outputDir = project.getLayout()
											 .getProjectDirectory()
											 .dir("src/" + sourceSetName + "/generated");

				final var generateTask = project.getTasks()
												.register(taskName, GenerateLmfSources.class, task -> {
													task.setGroup("LMF");
													task.setDescription(
															"Generates Java sources from LMF model files for source set "
															+ sourceSetName);

													task.getOutputDir().set(outputDir);

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
}
