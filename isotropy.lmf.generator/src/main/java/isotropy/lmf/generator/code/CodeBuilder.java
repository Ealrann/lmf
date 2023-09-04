package isotropy.lmf.generator.code;

public interface CodeBuilder<Input, Built>
{
	Built build(Input input);
}
