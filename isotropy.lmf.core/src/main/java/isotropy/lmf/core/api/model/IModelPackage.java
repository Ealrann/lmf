package isotropy.lmf.core.api.model;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.LMObject;

import java.util.Optional;

public interface IModelPackage
{
	Model model();
	<T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group);

	<T> Optional<T> resolveEnumLiteral(Enum<T> anEnum, String word);
}
