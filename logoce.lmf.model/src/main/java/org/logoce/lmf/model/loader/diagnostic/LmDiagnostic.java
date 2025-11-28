package org.logoce.lmf.model.loader.diagnostic;

public record LmDiagnostic(int line,
						   int column,
						   int length,
						   int offset,
						   Severity severity,
						   String message)
{
	public enum Severity
	{
		INFO,
		WARNING,
		ERROR
	}
}

