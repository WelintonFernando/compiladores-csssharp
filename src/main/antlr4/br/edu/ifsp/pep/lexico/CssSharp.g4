grammar CssSharp;
import CssSharpLexico;

// Regra inicial - permite variáveis e regras CSS
programa: (declaracaoVariavel | regra | condicional | switchCase | atribuicao)+ EOF;

// Declaração de variável: >> nomeVar: valor
declaracaoVariavel: VAR_DEF PALAVRA DOIS_PONTOS valor;

atribuicao: PALAVRA DOIS_PONTOS valor PONTO_VIRGULA;

// Regras CSS normais
regra: seletor ABRE_CHAVES propriedades FECHA_CHAVES;

seletor: HASHTAG PALAVRA     // #id
       | PONTO PALAVRA       // .classe
       | TAG                 // div, body, etc
       | PALAVRA             // container, titulo, etc
       ;

propriedades: (ATRIBUTOS DOIS_PONTOS valores PONTO_VIRGULA)*;

// Suporta múltiplos valores: border: 1px solid #000000;
valores: valor+;

valor: STRING
     | PALAVRA
     | DIGITO
     | DIGITO_REAL
     | expressao
     | cor
          | medida
     ;
medida: DIGITO_REAL UNIDADE_MEDIDA
      | DIGITO UNIDADE_MEDIDA
      | DIGITO
      ;

// Estruturas condicionais
condicional: PALAVRA_CHAVE ifStatement elseifStatement* elseStatement? PALAVRA_CHAVE;

ifStatement: ( expressaoLogica | PALAVRA) DOIS_PONTOS regra;  // %if tema == 'escuro':

expressaoLogica: (PALAVRA | STRING | DIGITO | DIGITO_REAL) OP_LOG (PALAVRA | STRING | DIGITO | DIGITO_REAL);

expressaoAritmetica: termo ((OP_ARIT) termo)*;
termo: fator (OP_PRIMARIO fator)*;
fator: DIGITO | DIGITO_REAL | PALAVRA | ABRE_PAR expressaoAritmetica FECHA_PAR;

elseifStatement: PALAVRA_CHAVE ifStatement;  // %elseif

elseStatement: PALAVRA_CHAVE DOIS_PONTOS regra;  // %else

// Switch case
switchCase: PALAVRA_CHAVE PALAVRA DOIS_PONTOS caseStatement+ defaultStatement? PALAVRA_CHAVE;  // %switch ... %endswitch

caseStatement: PALAVRA_CHAVE PALAVRA DOIS_PONTOS regra;  // %case escuro:

defaultStatement: PALAVRA_CHAVE DOIS_PONTOS regra;  // %default:

expressao: expressaoAritmetica
         | expressaoLogica
         ;

// Funções de cor RGB/RGBA
cor: PALAVRA_CHAVE ABRE_PAR DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA DIGITO_REAL FECHA_PAR  // rgb(255,255,255)
   | PALAVRA_CHAVE ABRE_PAR DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA ALPHA_VALUE FECHA_PAR  // rgba(...)
   | HEX;  // #FF00FF

//funcao: PALAVRA_CHAVE ABRE_PAR expressao FECHA_PAR;

//expressao: DIGITO_REAL
//         | DIGITO
//         | PALAVRA
//         | STRING
//         ;

//VAR           : '>>' PALAVRA  ':' (COR | DIGITO (DIGITO)* UNIDADE_MEDIDA| STRING);
//EXPRESSAO     : ( EXPRESSAO_LOGICA | EXPRESSAO_ARITMETICA );
//EXPRESSAO_ARITMETICA: TERMO (('+'|'-') TERMO)* ;
//TERMO         : FATOR (('*'|'/') FATOR)*  ;
//FATOR         : DIGITO | PALAVRA | '(' EXPRESSAO_ARITMETICA ')';
//OP_REL        : ('>'|'<'|'=='|'!='|'<='|'>=');
//EXPRESSAO_LOGICA: EXPRESSAO_ARITMETICA OP_REL EXPRESSAO_ARITMETICA;
//ATRIBUICAO  : PALAVRA ':' EXPRESSAO;
//IF_STMT     : EXPRESSAO CODIGO (('%elseif' IF_STMT|'%else') CODIGO)?;
//IF          :  '%if' IF_STMT '%endif';
//SWITCH      : '%switch'EXPRESSAO CASE_STMT ('default:' CODIGO)? '%endswitch';
//CASE_STMT   : '%case' VAR ':' CODIGO (CASE_STMT)?;
// CODIGO      : TAG_GERAL '{' ATRIBUTOS '}';
//TAG_GERAL   : ('.' | '#')? TAG ((',')? ('.'| '#')? TAG)*;
