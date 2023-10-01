package org.logoce.lmf.model.resource.transform.parsing;

import java.util.List;

public record ParsedNode(ParsedToken type, List<ParsedToken> values)
{}
