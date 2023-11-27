package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;

import java.util.Optional;

public interface IModelPackage
{
	MetaModel model();
	<T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group);

	<T> Optional<T> resolveEnumLiteral(Enum<T> anEnum, String word);
}
