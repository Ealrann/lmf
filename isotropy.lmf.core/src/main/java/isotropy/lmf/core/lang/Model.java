package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.lang.impl.RelationImpl;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;
import java.util.function.Supplier;

public interface Model extends Named
{
	List<Group<?>> groups();
	List<Enum<?>> enums();
	List<Unit<?>> units();
	List<Alias> aliases();

	IModelPackage lPackage();

	static Builder builder() {return new ModelBuilder();}
	interface Builder extends LMObject.Builder<Model>
	{
		Builder name(String name);
		Builder lPackage(IModelPackage modelPackage);

		Builder addGroup(Supplier<? extends Group<?>> group);
		Builder addEnum(Supplier<? extends Enum<?>> _enum);
		Builder addUnit(Supplier<? extends Unit<?>> unit);
		Builder addAlias(Supplier<Alias> alias);
	}
}
