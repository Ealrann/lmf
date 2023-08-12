package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Unit<T> extends Datatype<T>
{
	String matcher();
	String defaultValue();
	Primitive primitive();
	String extractor();

	Group<Unit<?>> GROUP = LMCorePackage.UNIT_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	interface Features
	{
		Attribute<String, String> Name = Named.Features.name;
		Attribute<String, String> Matcher = new AttributeImpl<>("matcher", true, false, false, LMCorePackage.MATCHER_UNIT);
		Attribute<String, String> DefaultValue = new AttributeImpl<>("defaultValue",
															 true,
															 false,
															 false,
															 LMCorePackage.STRING_UNIT);
		Attribute<Primitive, Primitive> Primitive = new AttributeImpl<>("primitive",
															 true,
															 false,
															 true,
															 LMCorePackage.PRIMITIVE_ENUM);
		Attribute<String, String> Extractor = new AttributeImpl<>("extractor",
														  true,
														  false,
														  false,
														  LMCorePackage.EXTRACTOR_UNIT);

		List<Feature<?, ?>> All = List.of(Name, Matcher, DefaultValue, Primitive, Extractor);
	}

	static Model.Builder builder() { return new ModelBuilder();}
	interface Builder<T> extends LMObject.Builder<Unit<T>>
	{
		Builder<T> name(String name);
		Builder<T> matcher(String matcher);
		Builder<T> defaultValue(String defaultValue);
		Builder<T> primitive(Primitive primitive);
		Builder<T> extractor(String extractor);
	}
}
