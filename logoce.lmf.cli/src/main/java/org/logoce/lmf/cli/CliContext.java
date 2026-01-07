package org.logoce.lmf.cli;

import java.io.PrintWriter;
import java.nio.file.Path;

public record CliContext(Path projectRoot, PrintWriter out, PrintWriter err)
{
}
