package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.loader.api.loader.model.LmDocument;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public record ModelLoadResult(Path path,
							  LmDocument document,
							  ModelHeader header,
							  List<File> metaModelFiles)
{
}
