package org.logoce.lmf.intellij;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LmLspServerSupportProvider implements LspServerSupportProvider
{
	@Override
	public void fileOpened(@NotNull final Project project,
						   @NotNull final VirtualFile file,
						   @NotNull final LspServerStarter serverStarter)
	{
		if (!"lm".equalsIgnoreCase(file.getExtension()))
		{
			return;
		}

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
			final String basePath = getProject().getBasePath();
			if (basePath != null && !basePath.isBlank())
			{
				return new GeneralCommandLine(List.of("logoce.lmf.lsp", "--project-root", basePath));
			}

			return new GeneralCommandLine(List.of("logoce.lmf.lsp"));
		}
	}
}
