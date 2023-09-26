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

LIST_SEPARATOR=[,]
TYPE=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*
VALUE=[a-zA-Z_+\-./\[\]][a-zA-Z0-9_+\-./\[\]*]*
WHITE_SPACE=[ \t\n\x0B\f\r]+
NAME=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*[=]

%state WAITING_TYPE
%state WAITING_VALUE
%state WAITING_LIST_VALUE

%%

<YYINITIAL> {

	{NAME}              { yybegin(WAITING_VALUE); return LMTokenTypes.NAME;	}
	{VALUE}       	    { yybegin(WAITING_LIST_VALUE); return LMTokenTypes.VALUE; }
	"(" 			    { yybegin(WAITING_TYPE); return LMTokenTypes.OPEN_NODE; }
	")"				    { return LMTokenTypes.CLOSE_NODE; }
	{WHITE_SPACE}       { return WHITE_SPACE; }
}

<WAITING_TYPE> {TYPE}   { yybegin(YYINITIAL); return LMTokenTypes.TYPE; }

<WAITING_VALUE> {
    {VALUE}             { yybegin(WAITING_LIST_VALUE); return LMTokenTypes.VALUE; }
}

<WAITING_LIST_VALUE> {
	{LIST_SEPARATOR}    { return LMTokenTypes.LIST_SEPARATOR;}
	{WHITE_SPACE}       { yybegin(YYINITIAL); return WHITE_SPACE; }
	")"				    { yybegin(YYINITIAL); return LMTokenTypes.CLOSE_NODE; }
    {VALUE}             { return LMTokenTypes.VALUE; }
}

[^] { return BAD_CHARACTER; }