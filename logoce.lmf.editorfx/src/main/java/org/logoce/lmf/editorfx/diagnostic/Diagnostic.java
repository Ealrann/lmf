package org.logoce.lmf.editorfx.diagnostic;

import java.nio.file.Path;

public final class Diagnostic {
	private final Path path;
	private final int line;   // 1-based
	private final int column; // 1-based
	private final int length;
	private final DiagnosticSeverity severity;
	private final String message;

	public Diagnostic(Path path, int line, int column, int length, DiagnosticSeverity severity, String message) {
		this.path = path;
		this.line = line;
		this.column = column;
		this.length = length;
		this.severity = severity;
		this.message = message;
	}

	public Path path() {
		return path;
	}

	public int line() {
		return line;
	}

	public int column() {
		return column;
	}

	public int length() {
		return length;
	}

	public DiagnosticSeverity severity() {
		return severity;
	}

	public String message() {
		return message;
	}
}
