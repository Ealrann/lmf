package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.logoce.lmf.core.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.linking.LmModelLinker;
import org.logoce.lmf.core.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main
{
	private Main()
	{
	}

	public static void main(final String[] args) throws Exception
	{
		if (args.length >= 2 && "--analyze".equals(args[0]))
		{
			runAnalyze(List.of(args).subList(1, args.length));
		}
		else
		{
			startServer(System.in, System.out, args);
		}
	}

	public static void startServer(final InputStream in, final OutputStream out) throws Exception
	{
		startServer(in, out, new String[0]);
	}

	public static void startServer(final InputStream in,
								   final OutputStream out,
								   final String[] args) throws Exception
	{
		java.nio.file.Path projectRoot = null;
		for (int i = 0; i < args.length; i++)
		{
			if ("--project-root".equals(args[i]) && i + 1 < args.length)
			{
				projectRoot = Path.of(args[i + 1]);
				break;
			}
		}

		final var server = new LmLanguageServer(projectRoot);
		final var launcher = LSPLauncher.createServerLauncher(server, in, out);
		final LanguageClient client = launcher.getRemoteProxy();
		server.connect(client);
		launcher.startListening().get();
	}

	private static void runAnalyze(final List<String> paths) throws Exception
	{
		if (paths.isEmpty())
		{
			System.err.println("Usage: Main --analyze <file1.lm> [file2.lm ...]");
			return;
		}

		for (final String pathString : paths)
		{
			final Path path = Path.of(pathString);
			final String text = Files.readString(path, StandardCharsets.UTF_8);

			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var treeReader = new LmTreeReader();
			final var readResult = treeReader.read(text, diagnostics);

			final var roots = readResult.roots();
			final var source = readResult.source();

			if (!roots.isEmpty())
			{
				final var linker = new LmModelLinker<PNode>(
						ModelRegistry.empty());
				linker.linkModel(roots, diagnostics, source);
			}

			if (diagnostics.isEmpty())
			{
				System.out.println(path + ": OK");
			}
			else
			{
				System.out.println(path + ":");
				for (final var d : diagnostics)
				{
					System.out.printf("  %d:%d [%s] %s%n",
									  d.line(),
									  d.column(),
									  d.severity(),
									  d.message());
				}
			}
		}
	}
}
