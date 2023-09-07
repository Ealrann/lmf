package isotropy.lmf.generator.code.util;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class InterfaceBuilder<Input> implements CodeBuilder<Input, TypeSpec>
{
	private final Function<Input, String> nameSupplier;
	private final StrongTypedBuilder<Input, ?, ?> typedBuilder;

	public <CodeInput, Output> InterfaceBuilder(final Function<Input, String> nameSupplier,
												final Function<Input, CodeBuilder<CodeInput, Output>> builderSupplier,
												final Function<Input, Stream<CodeInput>> extractor,
												final BiConsumer<TypeSpec.Builder, Output> installer,
												final BiConsumer<List<Output>, TypeSpec.Builder> postOperation)
	{
		this.nameSupplier = nameSupplier;
		typedBuilder = new StrongTypedBuilder<>(builderSupplier, extractor, installer, postOperation);
	}

	@Override
	public TypeSpec build(Input input)
	{
		final var name = nameSupplier.apply(input);
		final var interfaceBuilder = TypeSpec.interfaceBuilder(name).addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		typedBuilder.install(interfaceBuilder, input);
		return interfaceBuilder.build();
	}

	private record StrongTypedBuilder<Input, CodeInput, Output>(Function<Input, CodeBuilder<CodeInput, Output>> builderSupplier,
																Function<Input, Stream<CodeInput>> extractor,
																BiConsumer<TypeSpec.Builder, Output> installer,
																BiConsumer<List<Output>, TypeSpec.Builder> postOperation)
	{
		public void install(TypeSpec.Builder interfaceBuilder, Input input)
		{
			final Function<Output, Output> finalInstaller = output -> getOutput(interfaceBuilder, output);
			final var builder = builderSupplier.apply(input);
			final var result = extractor.apply(input).map(builder::build).map(finalInstaller).toList();
			postOperation.accept(result, interfaceBuilder);
		}

		private Output getOutput(final TypeSpec.Builder interfaceBuilder, final Output output)
		{
			installer.accept(interfaceBuilder, output);
			return output;
		}
	}
}
