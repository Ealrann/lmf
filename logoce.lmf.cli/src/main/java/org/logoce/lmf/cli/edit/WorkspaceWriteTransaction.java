package org.logoce.lmf.cli.edit;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkspaceWriteTransaction
{
	private final Map<Path, String> writes = new LinkedHashMap<>();

	public void put(final Path path, final String content)
	{
		Objects.requireNonNull(path, "path");
		writes.put(path.toAbsolutePath().normalize(), content == null ? "" : content);
	}

	public boolean isEmpty()
	{
		return writes.isEmpty();
	}

	public boolean commit(final PrintWriter err)
	{
		Objects.requireNonNull(err, "err");

		if (writes.isEmpty())
		{
			return true;
		}

		final var newTemps = new LinkedHashMap<Path, Path>();
		final var backups = new LinkedHashMap<Path, Path>();
		final var appliedTargets = new ArrayList<Path>();

		try
		{
			for (final var entry : writes.entrySet())
			{
				final var target = entry.getKey();
				final var parent = target.getParent();
				if (parent == null)
				{
					err.println("Cannot write file without parent directory: " + target);
					return false;
				}

				final var temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
				Files.writeString(temp, entry.getValue(), StandardCharsets.UTF_8);
				newTemps.put(target, temp);
			}

			for (final var entry : newTemps.entrySet())
			{
				final var target = entry.getKey();
				final var temp = entry.getValue();
				final var parent = target.getParent();
				if (parent == null)
				{
					err.println("Cannot write file without parent directory: " + target);
					return false;
				}

				final var backup = Files.createTempFile(parent, target.getFileName().toString(), ".bak");
				backups.put(target, backup);

				if (Files.exists(target))
				{
					move(target, backup);
				}

				move(temp, target);
				appliedTargets.add(target);
			}

			for (final var backup : backups.values())
			{
				Files.deleteIfExists(backup);
			}
			return true;
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to write file(s): " + message);
			rollback(appliedTargets, backups, err);
			return false;
		}
		finally
		{
			for (final var temp : newTemps.values())
			{
				try
				{
					Files.deleteIfExists(temp);
				}
				catch (Exception ignored)
				{
				}
			}
		}
	}

	private static void rollback(final List<Path> appliedTargets,
								 final Map<Path, Path> backups,
								 final PrintWriter err)
	{
		for (int i = appliedTargets.size() - 1; i >= 0; i--)
		{
			final var target = appliedTargets.get(i);
			final var backup = backups.get(target);
			if (backup == null || !Files.exists(backup))
			{
				continue;
			}

			try
			{
				move(backup, target);
			}
			catch (Exception e)
			{
				final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
				err.println("Failed to rollback file: " + target + " (" + message + ")");
			}
		}

		for (final var backup : backups.values())
		{
			try
			{
				Files.deleteIfExists(backup);
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private static void move(final Path from, final Path to) throws Exception
	{
		try
		{
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		}
		catch (AtomicMoveNotSupportedException e)
		{
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}

