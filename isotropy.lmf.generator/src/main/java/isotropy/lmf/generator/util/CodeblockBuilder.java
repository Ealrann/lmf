package isotropy.lmf.generator.util;

import com.squareup.javapoet.CodeBlock;

import java.util.function.Function;

public final class CodeblockBuilder<Input>
{
	final CodeBlock.Builder codeblockBuidler = CodeBlock.builder();

	final String separator;
	private final Function<Input, CodeBlock> feeder;

	boolean first = true;

	public CodeblockBuilder(final String separator, Function<Input, CodeBlock> feeder)
	{
		this.separator = separator;
		this.feeder = feeder;
	}

	public void feed(Input input)
	{
		if (first) first = false;
		else codeblockBuidler.add(",");

		codeblockBuidler.add(feeder.apply(input));
	}

	public CodeBlock build()
	{
		return codeblockBuidler.build();
	}
}
