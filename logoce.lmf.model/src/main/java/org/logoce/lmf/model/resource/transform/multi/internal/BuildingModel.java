package org.logoce.lmf.model.resource.transform.multi.internal;

import org.logoce.lmf.model.lang.Model;

public final class BuildingModel
{
	private final LoadModel model;
	private Model builtModel = null;

	public BuildingModel(final LoadModel model)
	{
		this.model = model;
	}

	public LoadModel LoadModel()
	{
		return model;
	}

	public void setBuiltModel(final Model builtModel)
	{
		this.builtModel = builtModel;
	}

	public LoadModel model()
	{
		return model;
	}

	public Model builtModel()
	{
		return builtModel;
	}
}
