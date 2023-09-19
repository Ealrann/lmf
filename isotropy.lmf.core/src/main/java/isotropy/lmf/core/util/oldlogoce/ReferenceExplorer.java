package isotropy.lmf.core.util.oldlogoce;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.lang.LMObject;

import java.util.List;
import java.util.stream.Stream;

public record ReferenceExplorer(RawFeature<?, ?> reference)
{
	@SuppressWarnings("unchecked")
	public Stream<LMObject> stream(LMObject object)
	{
		final var val = getValue(object);
		if (val instanceof List)
		{
			return ((List<LMObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((LMObject) val);
		}
	}

	private Object getValue(LMObject target)
	{
		return target.get(reference.featureSupplier().get());
	}
}
