package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.model.resource.diagnostic.ParseDiagnostic;

import java.io.File;
import java.util.List;
import java.util.Optional;

public record ModelInspectionResult(File file,
									Optional<String> qualifiedName,
									List<String> imports,
									List<ParseDiagnostic> diagnostics)
{
	public boolean hasMetaModel()
	{
		return qualifiedName.isPresent();
	}
}
