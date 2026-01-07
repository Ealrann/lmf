package org.logoce.lmf.core.loader.api.tooling.state;

public record ModelKey(String domain, String name)
{
	public String qualifiedName()
	{
		return (domain == null || domain.isBlank()) ? name : domain + "." + name;
	}
}
