package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.model.RawFeature;

public interface Unit<T> extends Datatype<T>
{
	String matcher();
	String defaultValue();
	Primitive primitive();
	String extractor();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<String, String> matcher = new RawFeature<>(false,
															  false,
															  () -> LMCoreDefinition.Features.UNIT.matcher);
		RawFeature<String, String> defaultValue = new RawFeature<>(false,
																   false,
																   () -> LMCoreDefinition.Features.UNIT.defaultValue);
		RawFeature<Primitive, Primitive> primitive = new RawFeature<>(false,
																	  false,
																	  () -> LMCoreDefinition.Features.UNIT.primitive);
		RawFeature<String, String> extractor = new RawFeature<>(false,
																false,
																() -> LMCoreDefinition.Features.UNIT.extractor);
	}

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
