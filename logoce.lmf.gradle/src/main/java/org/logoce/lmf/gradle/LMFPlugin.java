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

		extension.getModelDir().convention(project.getLayout()
			.getProjectDirectory().dir("src/main/model"));
		extension.getOutputDir().convention(project.getLayout()
			.getProjectDirectory().dir("src/main/generated"));
		extension.getIncludes().convention(List.of("**/*.lm"));

		project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
			final var javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
			final var mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

			final var generateTask = project.getTasks().register("generateLmf", GenerateLmfSources.class, task -> {
				task.setGroup("LMF");
				task.setDescription("Generates Java sources from LMF model files");

				task.getOutputDir().set(extension.getOutputDir());

				task.getModelFiles().from(project.fileTree(extension.getModelDir().get().getAsFile(), spec -> {
					final var includes = extension.getIncludes().get();
					spec.include(includes);
				}));
			});

			mainSourceSet.getJava().srcDir(extension.getOutputDir());

			project.getTasks().named(mainSourceSet.getCompileJavaTaskName())
				.configure(task -> task.dependsOn(generateTask));
		});
	}
}
