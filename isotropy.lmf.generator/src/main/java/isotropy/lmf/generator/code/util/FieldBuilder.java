package isotropy.lmf.generator.code.util;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FieldBuilder<Input> extends InterfaceBuilder<Input>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};

	public <CodeInput> FieldBuilder(final String name,
									final CodeBuilder<CodeInput, FieldSpec> builder,
									final Function<Input, Stream<CodeInput>> extractor,
									final AllListBuilder allBuilder)
	{
		super(i -> name,
			  i -> builder,
			  extractor,
			  TypeSpec.Builder::addField,
			  allBuilder != null ? allBuilder::postOperation : (n, l) -> {});
	}

	public <CodeInput> FieldBuilder(final Function<Input, String> nameSupplier,
									final Function<Input, CodeBuilder<CodeInput, FieldSpec>> builderSupplier,
									final Function<Input, Stream<CodeInput>> extractor,
									final AllListBuilder allBuilder)
	{
		super(nameSupplier,
			  builderSupplier,
			  extractor,
			  TypeSpec.Builder::addField,
			  allBuilder != null ? allBuilder::postOperation : (n, l) -> {});
	}

	public record AllListBuilder(TypeParameter builtType)
	{
		private void postOperation(List<FieldSpec> outputs, TypeSpec.Builder interfaceBuilder)
		{
			final var typedList = TypeParameter.of(ConstantTypes.LIST, builtType.parametrizedWildcard());
			final var listBlock = CodeBlock.builder().add("$T.of(", ConstantTypes.LIST);
			boolean first = true;
			for (final var output : outputs)
			{
				if (first) first = false;
				else listBlock.add(",");
				listBlock.add(output.name);
			}
			listBlock.add(")");
			interfaceBuilder.addField(FieldSpec.builder(typedList.parametrized(), "ALL", modifiers)
											   .initializer(listBlock.build())
											   .build());
		}
	}
}
