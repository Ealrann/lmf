package org.logoce.lmf.core.loader.api.tooling.workspace;

import java.util.Set;

/**
 * Default workspace scan exclusions for tooling that discovers {@code .lm} files on disk.
 * <p>
 * These are directory names that commonly contain build outputs or tool caches that would otherwise
 * duplicate source models (for example Gradle copying {@code src/<sourceSet>/model} into {@code build/resources}).
 */
public final class WorkspaceScanDefaults
{
	private static final Set<String> IGNORED_DIRECTORY_NAMES = Set.of(
		"build",
		".gradle",
		".git",
		".idea",
		".vscode",
		"out",
		"target",
		"node_modules");

	private WorkspaceScanDefaults()
	{
	}

	public static boolean isIgnoredDirectoryName(final String directoryName)
	{
		if (directoryName == null || directoryName.isBlank())
		{
			return false;
		}
		return IGNORED_DIRECTORY_NAMES.contains(directoryName);
	}
}
