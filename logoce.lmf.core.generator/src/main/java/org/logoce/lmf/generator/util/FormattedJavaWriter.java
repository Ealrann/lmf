package org.logoce.lmf.generator.util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FormattedJavaWriter
{
	private FormattedJavaWriter()
	{
	}

	public static void write(final JavaFile javaFile, final File targetDirectory)
	{
		final var formattedSource = format(javaFile);
		final var outputPath = resolvePath(javaFile, targetDirectory.toPath());

		try
		{
			final var parent = outputPath.getParent();
			if (parent != null)
			{
				Files.createDirectories(parent);
			}

			Files.writeString(outputPath, formattedSource, StandardCharsets.UTF_8);
		}
		catch (java.io.IOException e)
		{
			throw new UncheckedIOException("Failed to write Java file " + outputPath, e);
		}
	}

	private static String format(final JavaFile javaFile)
	{
		final var rawSource = javaFile.toString();
		final var typeSpec = javaFile.typeSpec;

		String result = rawSource;

		if (typeSpec.kind == TypeSpec.Kind.ENUM)
		{
			result = removeBlankLinesBetweenEnumConstants(result);
		}

		if (typeSpec.kind == TypeSpec.Kind.CLASS && isImplOrBuilder(typeSpec.name))
		{
			result = removeBlankLinesBetweenFields(result);
		}

		if (typeSpec.kind == TypeSpec.Kind.INTERFACE)
		{
			result = removeBlankLinesBetweenInterfaceMethods(result);
		}

		return result;
	}

	private static boolean isImplOrBuilder(final String typeName)
	{
		return typeName.endsWith("Impl") || typeName.endsWith("Builder");
	}

	private static String removeBlankLinesBetweenFields(final String source)
	{
		final var lines = source.split("\\R", -1);
		final var builder = new StringBuilder(source.length());

		for (int i = 0; i < lines.length; i++)
		{
			final var line = lines[i];
			if (line.trim().isEmpty() && i > 0 && i < lines.length - 1)
			{
				final var previous = lines[i - 1];
				final var next = lines[i + 1];
				if (isFieldLine(previous) && isFieldLine(next)) continue;
			}

			builder.append(line);
			if (i < lines.length - 1) builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

	private static boolean isFieldLine(final String line)
	{
		final var trimmed = line.trim();
		if (trimmed.isEmpty()) return false;
		if (!trimmed.endsWith(";")) return false;
		return trimmed.startsWith("private ") || trimmed.startsWith("public ") || trimmed.startsWith("protected ");
	}

	private static String removeBlankLinesBetweenInterfaceMethods(final String source)
	{
		final var lines = source.split("\\R", -1);
		final var builder = new StringBuilder(source.length());

		for (int i = 0; i < lines.length; i++)
		{
			final var line = lines[i];
			if (line.trim().isEmpty() && i > 0 && i < lines.length - 1)
			{
				final var previous = lines[i - 1];
				final var next = lines[i + 1];
				if (isInterfaceMemberLine(previous) && isInterfaceMemberLine(next)) continue;
			}

			builder.append(line);
			if (i < lines.length - 1) builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

	private static boolean isInterfaceMemberLine(final String line)
	{
		return isInterfaceMethodLine(line) || isInterfaceFieldLine(line);
	}

	private static boolean isInterfaceMethodLine(final String line)
	{
		final var trimmed = line.trim();
		if (trimmed.isEmpty()) return false;
		if (!trimmed.endsWith(");")) return false;
		if (!trimmed.contains("(")) return false;
		if (trimmed.contains("=")) return false;
		return true;
	}

	private static boolean isInterfaceFieldLine(final String line)
	{
		final var trimmed = line.trim();
		if (trimmed.isEmpty()) return false;
		if (!trimmed.endsWith(";")) return false;
		if (!trimmed.contains("=")) return false;
		if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) return false;
		if (trimmed.startsWith("public interface ") || trimmed.startsWith("interface ")) return false;
		if (trimmed.endsWith("{") || trimmed.equals("}")) return false;
		return true;
	}

	private static String removeBlankLinesBetweenEnumConstants(final String source)
	{
		final var lines = source.split("\\R", -1);
		final var builder = new StringBuilder(source.length());

		boolean inConstants = false;

		for (int i = 0; i < lines.length; i++)
		{
			final var line = lines[i];
			final var trimmed = line.trim();

			final var isEnumDeclarationLine = trimmed.contains(" enum ") && trimmed.endsWith("{");
			if (!inConstants && isEnumDeclarationLine)
			{
				inConstants = true;
			}

			if (inConstants && !isEnumDeclarationLine && (trimmed.startsWith("private ")
														  || trimmed.startsWith("public ")
														  || trimmed.startsWith("protected ")))
			{
				inConstants = false;
			}

			if (inConstants && trimmed.isEmpty() && i > 0 && i < lines.length - 1)
			{
				final var previous = lines[i - 1];
				final var next = lines[i + 1];
				if (isEnumConstantEndLine(previous) && isEnumConstantStartLine(next)) continue;
			}

			builder.append(line);
			if (i < lines.length - 1) builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

	private static boolean isEnumConstantStartLine(final String line)
	{
		final var trimmed = stripTrailingLineComment(line).trim();
		if (trimmed.isEmpty()) return false;
		if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) return false;
		if (trimmed.startsWith("public enum ") || trimmed.startsWith("enum ")) return false;
		if (trimmed.startsWith("private ") || trimmed.startsWith("public ") || trimmed.startsWith("protected ")) return false;
		if (trimmed.startsWith("@")) return false;
		if (trimmed.equals("}") || trimmed.equals("{")) return false;

		final var first = trimmed.charAt(0);
		if (!Character.isJavaIdentifierStart(first)) return false;
		for (int i = 1; i < trimmed.length(); i++)
		{
			final char c = trimmed.charAt(i);
			if (Character.isJavaIdentifierPart(c)) continue;
			return Character.isWhitespace(c) || c == '(' || c == '{' || c == ',' || c == ';';
		}
		return true;
	}

	private static boolean isEnumConstantEndLine(final String line)
	{
		final var trimmed = stripTrailingLineComment(line).trim();
		if (trimmed.isEmpty()) return false;
		return trimmed.endsWith(",") || trimmed.endsWith(";") || trimmed.equals("},") || trimmed.equals("};");
	}

	private static String stripTrailingLineComment(final String line)
	{
		final int commentIndex = line.indexOf("//");
		if (commentIndex < 0) return line;
		return line.substring(0, commentIndex);
	}

	private static Path resolvePath(final JavaFile javaFile, final Path targetDirectory)
	{
		final var packageName = javaFile.packageName;
		final var typeName = javaFile.typeSpec.name + ".java";

		if (packageName == null || packageName.isEmpty()) return targetDirectory.resolve(typeName);

		final var packageSegments = packageName.split("\\.");
		final var targetSegments = targetDirectory.normalize()
												  .toString()
												  .split(File.separator.equals("\\") ? "\\\\" : File.separator);

		int overlap = 0;
		for (int k = Math.min(packageSegments.length, targetSegments.length); k >= 1; k--)
		{
			boolean match = true;
			for (int i = 0; i < k; i++)
			{
				final var pkg = packageSegments[i];
				final var targetSeg = targetSegments[targetSegments.length - k + i];
				if (!pkg.equals(targetSeg))
				{
					match = false;
					break;
				}
			}
			if (match)
			{
				overlap = k;
				break;
			}
		}

		Path directory = targetDirectory.normalize();
		for (int i = overlap; i < packageSegments.length; i++)
		{
			directory = directory.resolve(packageSegments[i]);
		}

		return directory.resolve(typeName);
	}
}
