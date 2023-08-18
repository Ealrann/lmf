package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;

public interface Unit<T> extends Datatype<T>
{
	String matcher();
	String defaultValue();
	Primitive primitive();
	String extractor();

	static Model.Builder builder() {return new ModelBuilder();}
	interface Builder<T> extends LMObject.Builder<Unit<T>>
	{
		Builder<T> name(String name);
		Builder<T> matcher(String matcher);
		Builder<T> defaultValue(String defaultValue);
		Builder<T> primitive(Primitive primitive);
		Builder<T> extractor(String extractor);
	}
}
