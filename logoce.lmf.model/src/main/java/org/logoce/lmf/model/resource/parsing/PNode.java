package org.logoce.lmf.model.resource.parsing;

import java.util.List;

public record PNode(ParsedToken type, List<ParsedToken> values)
{}
