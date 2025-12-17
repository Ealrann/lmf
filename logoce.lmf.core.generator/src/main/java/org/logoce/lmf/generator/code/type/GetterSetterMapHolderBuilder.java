package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.core.lang.Group;

import javax.lang.model.element.Modifier;

/**
 * Builds the static holder class that contains the GET_MAP and SET_MAP fields
 * for a generated implementation. This ensures lazy initialization of those
 * maps and avoids static initialization cycles.
 */
public final class GetterSetterMapHolderBuilder implements CodeBuilder<Group<?>, TypeSpec>
{
	private final GetMapFieldBuilder getMapFieldBuilder;
	private final SetMapFieldBuilder setMapFieldBuilder;

	public GetterSetterMapHolderBuilder(final GroupInterfaceType interfaceType)
	{
		this.getMapFieldBuilder = new GetMapFieldBuilder(interfaceType);
		this.setMapFieldBuilder = new SetMapFieldBuilder(interfaceType);
	}

	@Override
	public TypeSpec build(final Group<?> group)
	{
		return TypeSpec.classBuilder("Inserters")
					   .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
					   .addField(getMapFieldBuilder.build(group))
					   .addField(setMapFieldBuilder.build(group))
					   .build();
	}
}

