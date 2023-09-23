package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.psi.LMTokenSets;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%public
%class LMLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

// We define various Lexer rules as regular expressions first.

OPEN_LIST="("
CLOSE_LIST=")"
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
WHITE_SPACE=[ \t\n\x0B\f\r]+

// Initial state. We can specify mutiple states for more complex grammars. This corresponds to `modes` in ANTLR grammar.
%%

"(" { return WHITE_SPACE; }
")" { return WHITE_SPACE; }

{IDENTIFIER}       { return LMTokenSets.IDENTIFIER; } // This indicates that a character sequence which matches to the rule
                                            // identifier is encountered.
{WHITE_SPACE}      { return WHITE_SPACE; } // This indicates that a character sequence which matches to the rule
                                             // whitespace is encountered.


// If the character sequence does not match any of the above rules, we return BAD_CHARACTER which indicates that
// there is an error in the character sequence. This is used to highlight errors.
[^] { return BAD_CHARACTER; }