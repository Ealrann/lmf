package org.logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.lang.MetaModel;

@ModelExtender(scope = MetaModel.class)
@Adapter
public final class ModelResolution implements IAdapter
{
	public final ClassName modelDefinition;

	private ModelResolution(final MetaModel model)
	{
		this.modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
											 model.name() + "ModelDefinition");
	}
}
