lexer grammar CssSharpLexico;

ABRE_PAR      : '(';
FECHA_PAR     : ')';
VAR_DEF       : '>>';
VIRGULA       : ',';
DOIS_PONTOS   : ':';
ABRE_CHAVES   : '{';
FECHA_CHAVES  : '}';
PONTO_VIRGULA : ';';
PONTO         : '.';
HASHTAG       : '#';
OP_LOG        : '>' | '<' | '==' | '!=' | '>=' | '<=';
OP_PRIMARIO   : '*' | '/';
OP_ARIT       : '+' | '-' | OP_PRIMARIO;

PALAVRA_CHAVE : ('%if' | '%else'| '%elseif' | '%endif' |'%switch' | '%case' |'%endswitch'
                | '%default' | 'rgb' | 'rgba' );

TAG           : ('html' | 'head' | 'title' | 'body' | 'header' | 'footer' | 'nav'
                | 'section' | 'article' | 'aside' | 'main' | 'h1' | 'h2' | 'h3'
                | 'h4' | 'h5' | 'h6' | 'p' | 'b' | 'i' | 'strong' | 'em' | 'br'
                | 'blockquote' | 'code' | 'ul' | 'ol' | 'li' | 'a' | 'img' | 'figure'
                | 'figcaption' | 'table' | 'tr' | 'td' | 'th' | 'thead' | 'tbody'
                | 'tfoot' | 'form' | 'input' | 'textarea' | 'button' | 'select'
                | 'option' | 'label' | 'style' | 'link' | 'script' | 'div' | 'span'
                | 'iframe');

ATRIBUTOS     : ('top' | 'left' | 'width' | 'height' | 'font-size' | 'margin' | 'padding'
                | 'color' | 'background-color' | 'border' | 'display' | 'position'
                | 'float' | 'clear' | 'z-index' | 'overflow' | 'text-align'
                | 'font-weight' | 'font-style' | 'text-decoration');

UNIDADE_MEDIDA: 'px' | 'vw' | 'vh'  | 'pt' | 'rem' | 'em';

HEX           : HASHTAG HEX_VALUE;
fragment
HEX_VALUE     : (('A'..'F') | DIGITO) (('A'..'F') | DIGITO) (('A'..'F') | DIGITO)
                (('A'..'F') | DIGITO) (('A'..'F') | DIGITO) (('A'..'F') | DIGITO);

DIGITO_REAL   : ('+' | '-')? ('0'..'9')+ ('.' ('0'..'9')+)?;
DIGITO        : ('+' | '-')? ('0'..'9')+;

RBG_VALUE     : ( ('0'..'9')
                | ('1'..'9') ('0'..'9')
                | '1' ('0'..'9') ('0'..'9')
                | '2' ('0'..'4') ('0'..'9')
                | '2' '5' ('0'..'5'));

ALPHA_VALUE   : ('0.' ('0'..'9')*) | ('1.0');

STRING        : '\'' ( ESC_SEQ | ~('\''|'\\') )* '\'';
fragment
ESC_SEQ       : '\\\'';

PALAVRA       : (('a'..'z') | ('A'..'Z')) (('a'..'z') | ('A'..'Z') | ('0'..'9') | '_')*;

WS            : (' ' | '\t' | '\r' | '\n')+ {skip();};