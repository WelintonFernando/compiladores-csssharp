grammar CssSharp;
import CssSharpLexico;

ALPHA_VALUE   : ('0.' ('0'..'9')*) | ('1.0');
RBG_VALUE     : ( ('0'..'9')
                | ('1'..'9') ('0'..'9')
                | '1' ('0'..'9') ('0'..'9')
                | '2' ('0'..'4') ('0'..'9')
                | '2' '5' ('0'..'5'));


HEX           : HASHTAG HEX_VALUE;
HEX_VALUE     :(('A'..'F') | DIGITO) (('A'..'F') | DIGITO) (('A'..'F') | DIGITO)
               (('A'..'F') | DIGITO) (('A'..'F') | DIGITO) (('A'..'F') | DIGITO);

VAR           : '>>' PALAVRA  ':' (COR | DIGITO (DIGITO)* UNIDADE_MEDIDA| STRING);
EXPRESSAO     : (DIGITO | '\'' PALAVRA '\'' | EXPRESSAO_LOGICA | EXPRESSAO_ARITMETICA );
EXPRESSAO_ARITMETICA: TERMO (('+'|'-') TERMO)* ;
TERMO         : FATOR (('*'|'/') FATOR)*  ;
FATOR         : DIGITO | PALAVRA | '(' EXPRESSAO_ARITMETICA ')';
OP_REL        : ('>'|'<'|'=='|'!='|'<='|'>=');
EXPRESSAO_LOGICA: EXPRESSAO_ARITMETICA OP_REL EXPRESSAO_ARITMETICA;
ATRIBUICAO  : PALAVRA ':' EXPRESSAO;
IF_STMT     : EXPRESSAO CODIGO (('%elseif' IF_STMT|'%else') CODIGO)?;
IF          :  '%if' IF_STMT '%endif';
SWITCH      : '%switch'EXPRESSAO CASE_STMT ('default:' CODIGO)? '%endswitch';
CASE_STMT   : '%case' VAR ':' CODIGO (CASE_STMT)?;
CODIGO      : TAG_GERAL '{' ATRIBUTOS '}';
TAG_GERAL   : ('.' | '#')? TAG ((',')? ('.'| '#')? TAG)*;
TAG         : ('html' | 'head' | 'title' | 'body' | 'header' | 'footer' | 'nav'
                | 'section' | 'article' | 'aside' | 'main' | 'h1' | 'h2' | 'h3'
                | 'h4' | 'h5' | 'h6' | 'p' | 'b' | 'i' | 'strong' | 'em' | 'br'
                | 'blockquote' | 'code' | 'ul' | 'ol' | 'li' | 'a' | 'img' | 'figure'
                | 'figcaption' | 'table' | 'tr' | 'td' | 'th' | 'thead' | 'tbody'
                | 'tfoot' | 'form' | 'input' | 'textarea' | 'button' | 'select'
                | 'option' | 'label' | 'style' | 'link' | 'script' | 'div' | 'span'
                | 'iframe');
COR         :('rgb(' RBG_VALUE ',' RBG_VALUE ',' RBG_VALUE ')'
               |'rgba(' RBG_VALUE ',' RBG_VALUE ',' RBG_VALUE ',' ALPHA_VALUE ')'
               |HEX);
