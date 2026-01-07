package org.logoce.lmf.cli.edit;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

final class CustomValidationContext implements EditValidationContext
{
	private final BiFunction<Map<Path, String>, PrintWriter, Boolean> validator;

	CustomValidationContext(final BiFunction<Map<Path, String>, PrintWriter, Boolean> validator)
	{
		this.validator = Objects.requireNonNull(validator, "validator");
	}

	@Override
	public boolean validate(final Map<Path, String> sourcesByPath, final PrintWriter err)
	{
		return validator.apply(sourcesByPath, err);
	}
}
