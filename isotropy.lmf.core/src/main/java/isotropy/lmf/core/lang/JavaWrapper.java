package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.JavaWrapperBuilder;
import isotropy.lmf.core.model.RawFeature;

public interface JavaWrapper<T> extends Datatype<T>
{
	String domain();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<String, String> domain = new RawFeature<>(false,
															 false,
															 () -> LMCoreDefinition.Features.JAVA_WRAPPER.domain);
	}

	static <T> Builder<T> builder() {return new JavaWrapperBuilder<>();}
	interface Builder<T> extends LMObject.Builder<JavaWrapper<T>>
	{
		Builder<T> name(String name);
		Builder<T> domain(String domain);
	}
}
