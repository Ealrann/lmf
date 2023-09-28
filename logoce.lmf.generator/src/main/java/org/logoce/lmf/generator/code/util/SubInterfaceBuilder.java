package org.logoce.lmf.generator.code.util;

import com.squareup.javapoet.TypeSpec;

import java.util.function.Function;
import java.util.stream.Stream;

public final class SubInterfaceBuilder<Input> extends InterfaceBuilder<Input>
{
	public <CodeInput> SubInterfaceBuilder(final String name,
										   final CodeBuilder<CodeInput, TypeSpec> builder,
										   final Function<Input, Stream<CodeInput>> extractor)
	{
		super(i -> name, i -> builder, extractor, TypeSpec.Builder::addType, (r, t) -> {});
	}
}
