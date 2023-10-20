package org.logoce.lmf.model.resource.parsing;

import java.util.List;

public interface PNode
{
	List<PToken> tokens();

	record SimplePNode(List<PToken> tokens) implements PNode {}
}
