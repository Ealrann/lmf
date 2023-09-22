package logoce.lmf.model.api.model;

import logoce.lmf.model.lang.Enum;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.lang.LMObject;

import java.util.Optional;

public interface IModelPackage
{
	Model model();
	<T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group);

	<T> Optional<T> resolveEnumLiteral(Enum<T> anEnum, String word);
}
