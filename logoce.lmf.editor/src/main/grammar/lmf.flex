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
VALUE=[a-zA-Z0-9_+\-./\[\]*]+
WHITE_SPACE=[ \t\n\x0B\f\r]+
NAME=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*[=]
QUOTE=[\"]
ALL_EXCEPT_QUOTES = [^\"]
ASSIGN=[=]

%state WAITING_TYPE
%state ASSIGNED_VALUE
%state FORCED_VALUE

%%

<YYINITIAL> {

	{NAME}              { yybegin(ASSIGNED_VALUE); yypushback(1); return LMTokenTypes.NAME;	}
	{VALUE}       	    { return LMTokenTypes.VALUE; }
	{LIST_SEPARATOR}    { return LMTokenTypes.LIST_SEPARATOR;}

	"(" 			    { yybegin(WAITING_TYPE); return LMTokenTypes.OPEN_NODE; }
	")"				    { return LMTokenTypes.CLOSE_NODE; }
	{WHITE_SPACE}       { return WHITE_SPACE; }
    {QUOTE}             { yybegin(FORCED_VALUE); return LMTokenTypes.QUOTE; }
}

<WAITING_TYPE> {TYPE}   { yybegin(YYINITIAL); return LMTokenTypes.TYPE; }

<ASSIGNED_VALUE> {
    {ASSIGN}            { return LMTokenTypes.ASSIGN;	}
    {VALUE}             { yybegin(YYINITIAL); return LMTokenTypes.VALUE; }
    {QUOTE}             { yybegin(FORCED_VALUE); return LMTokenTypes.QUOTE; }
}

<FORCED_VALUE> {
    {ALL_EXCEPT_QUOTES}+ { return LMTokenTypes.VALUE; }
    {QUOTE}              { yybegin(YYINITIAL); return LMTokenTypes.QUOTE; }
}

[^] { return BAD_CHARACTER; }