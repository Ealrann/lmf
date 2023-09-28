package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.code.util.CodeBuilder;

import javax.lang.model.element.Modifier;

public interface DefinitionFieldBuilder<Type> extends CodeBuilder<Type, FieldSpec>
{
	Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
}
