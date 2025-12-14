package org.logoce.lmf.intellij;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LmLspServerSupportProvider implements LspServerSupportProvider
{
	private static final Logger LOG = Logger.getInstance(LmLspServerSupportProvider.class);

	@Override
	public void fileOpened(@NotNull final Project project,
						   @NotNull final VirtualFile file,
						   @NotNull final LspServerStarter serverStarter)
	{
		if (!"lm".equalsIgnoreCase(file.getExtension()))
		{
			return;
		}

		LOG.info("LMF LSP fileOpened: file=" + file.getPath() + ", basePath=" + project.getBasePath());
		serverStarter.ensureServerStarted(new LmLspServerDescriptor(project));
	}

	private static final class LmLspServerDescriptor extends ProjectWideLspServerDescriptor
	{
		private LmLspServerDescriptor(@NotNull final Project project)
		{
			super(project, "LMF LSP");
		}

		@Override
		public boolean isSupportedFile(@NotNull final VirtualFile file)
		{
			return "lm".equalsIgnoreCase(file.getExtension());
		}

		@Override
		public @NotNull GeneralCommandLine createCommandLine()
		{
			final String projectRoot = resolveProjectRoot(getProject());
			if (projectRoot != null && !projectRoot.isBlank())
			{
				LOG.info("LMF LSP starting: logoce.lmf.lsp --project-root " + projectRoot);
				return new GeneralCommandLine(List.of("logoce.lmf.lsp", "--project-root", projectRoot));
			}

			LOG.warn("LMF LSP starting: logoce.lmf.lsp (no project root detected)");
			return new GeneralCommandLine(List.of("logoce.lmf.lsp"));
		}

		private static String resolveProjectRoot(final Project project)
		{
			final String basePath = project.getBasePath();
			if (basePath != null && !basePath.isBlank())
			{
				return basePath;
			}

			final var guess = ProjectUtil.guessProjectDir(project);
			return guess != null ? guess.getPath() : null;
		}
	}
}
