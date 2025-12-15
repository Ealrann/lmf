package org.logoce.lmf.core.api.text.syntax;

import java.util.List;

public interface PNode
{
	List<PToken> tokens();

	record SimplePNode(List<PToken> tokens) implements PNode {}
}
