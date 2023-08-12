package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Unit<T> extends Datatype<T>
{
	String matcher();
	String defaultValue();
	Primitive primitive();
	String extractor();

	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

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
