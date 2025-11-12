lexer grammar CssSharpLexico;

DIGITO        : ('+' | '-')? ('0'..'9')+;
DIGITO_REAL   : ('+' | '-')? ('0'..'9')+ ('.' ('0'..'9')+)?;
PALAVRA      : (('a'..'z') | ('A'..'Z')) (('a'..'z') | ('A'..'Z') | ('0'..'9') | '_')*;
PALAVRA_CHAVE : ('%if' | '%else'| '%elseif' | '%endif' |'%switch' | '%case' |'%endswitch'
                | '%default:' | 'rgb' | 'rgba' );
STRING        : '\'' ( ESC_SEQ | ~('\''|'\\') )* '\'';
fragment
ESC_SEQ       : '\\\'';
ATRIBUTOS     : ('top' | 'left' | 'width' | 'height' | 'font-size' | 'margin' | 'padding');
ABRE_PAR      : '(';
FECHA_PAR     : ')';
VAR_DEF       : '>>';
OP_LOG        : '>' | '<' | '==' | '!=' | '>=' | '<=';
OP_ARIT       : '+' | '-' | '*' | '/';
WS            : (' ' | '\t' | '\r' | '\n')* {skip();};
UNIDADE_MEDIDA: 'px' | 'vw' | 'vh'  | 'pt' | 'rem' | 'em';
TAG           : 'div' | 'h1' | 'body' | 'p' | 'span' | 'header' | 'footer' | 'section';
HASHTAG       : '#';
