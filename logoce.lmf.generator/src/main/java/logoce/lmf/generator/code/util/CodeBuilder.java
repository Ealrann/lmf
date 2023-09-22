package logoce.lmf.generator.code.util;

public interface CodeBuilder<Input, Built>
{
	Built build(Input input);
}
