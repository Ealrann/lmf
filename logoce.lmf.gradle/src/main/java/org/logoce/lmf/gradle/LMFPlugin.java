package org.logoce.lmf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.plugins.JavaPluginExtension;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class LMFPlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		final var javaPlugin = project.getExtensions().getByType(JavaPluginExtension.class);
		final var sourceSets = javaPlugin.getSourceSets();
		final var javaSourceSet = sourceSets.getByName("main").getJava();

		javaSourceSet.srcDir(new File("src/main/model"));
		javaSourceSet.srcDir(new File("src/main/generated"));

		project.task("hello").doLast("Hello", task -> {

			final var visitor = new ModelFileVisitor();
			final var models = javaSourceSet.getSourceDirectories().getAsFileTree();
			models.visit(visitor);

			visitor.streamModels().forEach(m -> System.out.println("m = " + m));
		});
	}

	private static final class ModelFileVisitor implements FileVisitor
	{
		private final Set<File> models = new HashSet<>();

		public Stream<File> streamModels()
		{
			return models.stream();
		}

		@Override
		public void visitDir(final FileVisitDetails dirDetails)
		{
		}

		@Override
		public void visitFile(final FileVisitDetails fileDetails)
		{
			if (fileDetails.getName().endsWith(".lm"))
			{
				models.add(fileDetails.getFile());
			}
		}
	}
}
