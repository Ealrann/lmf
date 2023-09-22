module logoce.lmf.model
{
	requires transitive logoce.lmf.adapter;
	requires transitive logoce.lmf.extender;
	requires transitive logoce.lmf.notification;

	exports logoce.lmf.model.lang;
	exports logoce.lmf.model.lang.impl;
	exports logoce.lmf.model.resource;
	exports logoce.lmf.model.api.notification;
	exports logoce.lmf.model.api.feature;
	exports logoce.lmf.model.api.model;

	exports logoce.lmf.model.feature to logoce.lmf.generator;
	exports logoce.lmf.model.util to logoce.lmf.generator;
}
