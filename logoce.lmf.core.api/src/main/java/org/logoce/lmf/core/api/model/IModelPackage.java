package org.logoce.lmf.core.api.model;

import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.MetaModel;

import java.util.Optional;

public interface IModelPackage
{
	MetaModel model();
	<T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(Group<T> group);

	<T> Optional<T> resolveEnumLiteral(Enum<T> anEnum, String word);

	<T> Optional<IJavaWrapperConverter<T>> resolveJavaWrapperConverter(final JavaWrapper<T> wrapper);
}
