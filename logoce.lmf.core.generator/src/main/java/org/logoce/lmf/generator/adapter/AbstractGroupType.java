package org.logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.core.lang.Group;

import java.util.List;

public class AbstractGroupType implements TypeParameter
{
	public final String packageName;
	public final Group<?> group;
	public final List<TypeName> superInterfaces;
	public final List<TypeVariableName> detailedParameters;

	final TypeParameter mainClass;

	protected AbstractGroupType(final Values values)
	{
		this.mainClass = values.mainClass;
		this.packageName = values.packageName;
		this.group = values.group;
		this.superInterfaces = values.superInterfaces;
		this.detailedParameters = values.detailedParameters;
	}

	public TypeSpec.Builder interfaceSpecBuilder()
	{
		return TypeSpec.interfaceBuilder(mainClass.raw())
					   .addSuperinterfaces(superInterfaces)
					   .addTypeVariables(detailedParameters);
	}

	public TypeSpec.Builder classSpecBuilder()
	{
		return TypeSpec.classBuilder(mainClass.raw())
					   .addSuperinterfaces(superInterfaces)
					   .addTypeVariables(detailedParameters);
	}

	@Override
	public ClassName raw()
	{
		return mainClass.raw();
	}

	@Override
	public TypeName parametrized()
	{
		return mainClass.parametrized();
	}

	@Override
	public TypeName parametrizedWildcard()
	{
		return mainClass.parametrizedWildcard();
	}

	@Override
	public List<? extends TypeName> parameters()
	{
		return mainClass.parameters();
	}

	protected record Values(TypeParameter mainClass,
							String packageName,
							Group<?> group,
							List<TypeName> superInterfaces,
							List<TypeVariableName> detailedParameters) {}
}
