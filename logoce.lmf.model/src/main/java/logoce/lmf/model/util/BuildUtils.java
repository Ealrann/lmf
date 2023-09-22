package logoce.lmf.model.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class BuildUtils
{
	public static <T> List<T> collectSuppliers(List<Supplier<T>> suppliers)
	{
		return suppliers.stream().map(Supplier::get).toList();
	}
}
