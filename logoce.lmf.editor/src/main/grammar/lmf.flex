package org.logoce.lmf.editor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.logoce.lmf.editor.psi.LMTokenTypes;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%public
%class LMLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

OPEN_LIST="("
CLOSE_LIST=")"
IDENTIFIER=[a-zA-Z_+\-=][a-zA-Z0-9_+\-=]*
WHITE_SPACE=[ \t\n\x0B\f\r]+

%%

"(" { return LMTokenTypes.OPEN_NODE; }
")" { return LMTokenTypes.CLOSE_NODE; }

{IDENTIFIER}       { return LMTokenTypes.IDENTIFIER; }
{WHITE_SPACE}      { return WHITE_SPACE; }


[^] { return BAD_CHARACTER; }