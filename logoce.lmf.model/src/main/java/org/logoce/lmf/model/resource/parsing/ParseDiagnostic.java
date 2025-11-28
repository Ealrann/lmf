package org.logoce.lmf.model.resource.parsing;

public record ParseDiagnostic(int line, int column, int length, int offset, Severity severity, String message) {
	public enum Severity {
		INFO, WARNING, ERROR
	}
}
