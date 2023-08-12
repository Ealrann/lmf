package isotropy.lmf.core.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class BuildUtils
{
	public static <T> List<T> collectSuppliers(List<? extends Supplier<? extends T>> suppliers)
	{
		return suppliers.stream().map(Supplier::get).collect(Collectors.toUnmodifiableList());
	}
}
