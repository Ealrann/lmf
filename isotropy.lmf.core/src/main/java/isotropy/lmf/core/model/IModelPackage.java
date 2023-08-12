package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Model;

import java.util.Optional;

public interface IModelPackage
{
	Model model();
	<T extends LMObject> IFeaturedObject.Builder<T> builder(Group<T> group);

	<T> Optional<T> resolveEnum(Enum<T> anEnum, String word);
}
