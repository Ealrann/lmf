package org.logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import org.logoce.lmf.core.api.adapter.Adapter;
import org.logoce.lmf.core.api.extender.IAdapter;
import org.logoce.lmf.core.api.extender.ModelExtender;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.core.lang.MetaModel;

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
