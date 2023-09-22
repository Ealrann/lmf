package logoce.lmf.generator.code.model;

import com.squareup.javapoet.FieldSpec;
import logoce.lmf.generator.code.util.CodeBuilder;

import javax.lang.model.element.Modifier;

public interface DefinitionFieldBuilder<Type> extends CodeBuilder<Type, FieldSpec>
{
	Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
}
