module isotropy.lmf.core
{
	requires transitive logoce.adapter;
	requires transitive logoce.extender;
	requires transitive logoce.notification;

	exports isotropy.lmf.core.lang;
	exports isotropy.lmf.core.lang.impl;
	exports isotropy.lmf.core.resource;
	exports isotropy.lmf.core.api.notification;
	exports isotropy.lmf.core.api.feature;
	exports isotropy.lmf.core.api.model;

	exports isotropy.lmf.core.feature to isotropy.lmf.generator;
	exports isotropy.lmf.core.util to isotropy.lmf.generator;
}
