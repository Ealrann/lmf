package org.logoce.lmf.lsp.features.completion;

/**
 * Describes how a type is being used at the current completion position.
 * <ul>
 *   <li>{@link #DATATYPE} – a value for an Attribute's {@code datatype} feature.</li>
 *   <li>{@link #CONCEPT} – a value for a Relation's {@code concept} feature.</li>
 *   <li>{@link #ANY} – any other usage.</li>
 * </ul>
 */
enum TypeUsageKind
{
	ANY,
	DATATYPE,
	CONCEPT
}

