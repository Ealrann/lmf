package org.logoce.lmf.model.lexer;

@SuppressWarnings("ALL")
%%

%public
%class LMLexer
%function next
%type ELMTokenType
%unicode

LIST_SEPARATOR=[,]
TYPE=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*
TYPE_NAME=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*[=]
VALUE=[a-zA-Z0-9_+\-./\[\]*#]+
VALUE_NAME=[a-zA-Z_+\-][a-zA-Z0-9_+\-]*[=]
WHITE_SPACE=[ \t\n\x0B\f\r]+
QUOTE=[\"]
ALL_EXCEPT_QUOTES = [^\"]
ASSIGN=[=]

%state WAITING_TYPE
%state ASSIGNED_VALUE
%state FORCED_VALUE

%%

<YYINITIAL> {

	{VALUE_NAME}        { yybegin(ASSIGNED_VALUE); yypushback(1); return ELMTokenType.VALUE_NAME;	}
	{VALUE}       	    { return ELMTokenType.VALUE; }
	{LIST_SEPARATOR}    { return ELMTokenType.LIST_SEPARATOR;}

	"(" 			    { yybegin(WAITING_TYPE); return ELMTokenType.OPEN_NODE; }
	")"				    { return ELMTokenType.CLOSE_NODE; }
	{WHITE_SPACE}       { return ELMTokenType.WHITE_SPACE; }
    {QUOTE}             { yybegin(FORCED_VALUE); return ELMTokenType.QUOTE; }
}

<WAITING_TYPE> {
	{TYPE_NAME}         { yypushback(1); return ELMTokenType.TYPE_NAME; }
    {ASSIGN}            { return ELMTokenType.ASSIGN;	}
	{TYPE}              { yybegin(YYINITIAL); return ELMTokenType.TYPE; }
}

<ASSIGNED_VALUE> {
    {ASSIGN}            { return ELMTokenType.ASSIGN;	}
    {VALUE}             { yybegin(YYINITIAL); return ELMTokenType.VALUE; }
    {QUOTE}             { yybegin(FORCED_VALUE); return ELMTokenType.QUOTE; }
}

<FORCED_VALUE> {
    {ALL_EXCEPT_QUOTES}+ { return ELMTokenType.VALUE; }
    {QUOTE}              { yybegin(YYINITIAL); return ELMTokenType.QUOTE; }
}

[^] { return ELMTokenType.BAD_CHARACTER; }