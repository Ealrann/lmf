package isotropy.lmf.core.util.oldlogoce;

import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.List;
import java.util.stream.Stream;

public record ReferenceExplorer(int reference)
{
	@SuppressWarnings("unchecked")
	public Stream<ILilyEObject> stream(ILilyEObject object)
	{
		final var val = getValue(object);
		if (val instanceof List)
		{
			return ((List<ILilyEObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((ILilyEObject) val);
		}
	}

	private Object getValue(ILilyEObject target)
	{
		return target.eGet(reference, true, true);
	}
}
