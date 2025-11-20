# Compilador CSS#  
***  

## Sumário:
1. [Estrutura do projeto](#1-estrutura-do-projeto)
2. [Analisador Léxico](#2-analisador-léxico)
3. [Analisador Sintático](#3-analisador-sintático)
4. [Analisador Semântico](#4-analisador-semântico)
5. [Geração de Código](#5-geração-de-código)
6. [Como Usar](#6-como-usar)
7. [Exemplos](#7-exemplos)
8. [Autores e Licença](#8-autores-e-licença)

---

## Descrição do Projeto

O **CSS#** (CSS Sharp) é um compilador educacional que estende a linguagem CSS com recursos de programação, como variáveis, estruturas condicionais (`%if`, `%else`, `%elseif`) e estruturas de seleção (`%switch`/`%case`). O compilador transforma código CSS# em CSS puro e válido.

---

## 1. Estrutura do Projeto

```
compilador-css-sharp/
├── src/
│   ├── main/
│   │   ├── antlr4/
│   │   │   └── br/edu/ifsp/pep/lexico/
│   │   │       ├── CssSharp.g4           # Gramática sintática
│   │   │       └── CssSharpLexico.g4     # Gramática léxica
│   │   └── java/
│   │       └── br/edu/ifsp/pep/csss/
│   │           ├── Principal.java                 # Orquestrador principal
│   │           ├── TabelaDeSimbolos.java          # Gerenciador de símbolos
│   │           ├── CSSSharpSemanticoUtils.java    # Utilitários semânticos
│   │           ├── CSSSharpSemantico.java         # Análise semântica
│   │           └── CSSSharpGeradorCSS.java        # Gerador de CSS
│   │       └── file/
│   │           ├── teste.csss                     # Arquivo de teste principal
│   │           ├── teste_erro_variavel.csss       # Teste: variáveis não declaradas
│   │           ├── teste_erro_tipo.csss           # Teste: tipos incompatíveis
│   │           ├── teste_erro_duplicado.csss      # Teste: declarações duplicadas
│   │           └── teste_erro_condicional.csss    # Teste: erros em condicionais
├── target/
│   └── generated-sources/antlr4/          # Classes geradas pelo ANTLR
├── pom.xml                                 # Configuração Maven
└── readme.md                               # Este arquivo
```

---

## 2. Analisador Léxico

### 2.1 Gramática Léxica (`CssSharpLexico.g4`)

O analisador léxico é definido em uma gramática ANTLR4 separada e importada pela gramática sintática.

#### 2.1.1 Tokens de Estrutura

| Token | Símbolo | Descrição |
|-------|---------|-----------|
| `ABRE_PAR` | `(` | Abre parênteses |
| `FECHA_PAR` | `)` | Fecha parênteses |
| `ABRE_CHAVES` | `{` | Abre chaves |
| `FECHA_CHAVES` | `}` | Fecha chaves |
| `VAR_DEF` | `>>` | Definição de variável |
| `DOIS_PONTOS` | `:` | Separador chave-valor |
| `PONTO_VIRGULA` | `;` | Fim de declaração |
| `VIRGULA` | `,` | Separador de valores |
| `PONTO` | `.` | Seletor de classe |
| `HASHTAG` | `#` | Seletor de ID ou cor hex |

#### 2.1.2 Operadores

| Token | Símbolos | Descrição |
|-------|----------|-----------|
| `OP_LOG` | `>`, `<`, `==`, `!=`, `>=`, `<=` | Operadores lógicos/relacionais |
| `OP_PRIMARIO` | `*`, `/` | Operadores de multiplicação e divisão |
| `OP_ARIT` | `+`, `-`, `*`, `/` | Operadores aritméticos |

#### 2.1.3 Palavras-chave

| Token | Valores | Uso |
|-------|---------|-----|
| `PALAVRA_CHAVE` | `%if`, `%else`, `%elseif`, `%endif` | Estruturas condicionais |
|  | `%switch`, `%case`, `%endswitch`, `%default` | Estrutura switch/case |
|  | `rgb`, `rgba` | Funções de cor |

#### 2.1.4 Tags HTML Suportadas

**Token `TAG`**: Reconhece 42 tags HTML5 válidas:
- Estrutura: `html`, `head`, `title`, `body`, `header`, `footer`, `nav`, `section`, `article`, `aside`, `main`
- Cabeçalhos: `h1`, `h2`, `h3`, `h4`, `h5`, `h6`
- Texto: `p`, `b`, `i`, `strong`, `em`, `br`, `blockquote`, `code`
- Listas: `ul`, `ol`, `li`
- Mídia: `a`, `img`, `figure`, `figcaption`, `iframe`
- Tabelas: `table`, `tr`, `td`, `th`, `thead`, `tbody`, `tfoot`
- Formulários: `form`, `input`, `textarea`, `button`, `select`, `option`, `label`
- Container: `div`, `span`
- Meta: `style`, `link`, `script`

#### 2.1.5 Propriedades CSS Suportadas

**Token `ATRIBUTOS`**: 19 propriedades CSS essenciais:
- Posicionamento: `top`, `left`, `position`, `float`, `clear`, `z-index`
- Dimensões: `width`, `height`, `margin`, `padding`
- Tipografia: `font-size`, `font-weight`, `font-style`, `text-align`, `text-decoration`
- Visual: `color`, `background-color`, `border`, `display`, `overflow`

#### 2.1.6 Unidades de Medida

**Token `UNIDADE_MEDIDA`**: `px`, `vw`, `vh`, `pt`, `rem`, `em`

#### 2.1.7 Valores

| Token | Padrão | Exemplo |
|-------|--------|---------|
| `DIGITO` | `(+\|-)?[0-9]+` | `100`, `-50`, `+25` |
| `DIGITO_REAL` | `(+\|-)?[0-9]+(\.[0-9]+)?` | `1.5`, `-2.75`, `+10.0` |
| `HEX` | `#[A-F0-9]{6}` | `#FF00FF`, `#A1B2C3` |
| `STRING` | `'...'` | `'Arial'`, `'claro'` |
| `PALAVRA` | `[a-zA-Z][a-zA-Z0-9_]*` | `tema`, `corPrimaria` |
| `RBG_VALUE` | `0-255` | `255`, `128`, `0` |
| `ALPHA_VALUE` | `0.0-1.0` | `0.5`, `1.0`, `0.75` |

#### 2.1.8 Classes Geradas

As seguintes classes são **geradas automaticamente** pelo ANTLR4:
- `CssSharpLexer.java` - Analisador léxico gerado
- `CssSharpParser.java` - Analisador sintático gerado
- `CssSharpBaseVisitor.java` - Classe base para visitors
- `CssSharpVisitor.java` - Interface visitor
- `CssSharpListener.java` - Interface listener (não utilizada)
- `CssSharpBaseListener.java` - Classe base para listeners (não utilizada)

---

## 3. Analisador Sintático

### 3.1 Gramática Sintática (`CssSharp.g4`)

Define a estrutura hierárquica da linguagem CSS#.

#### 3.1.1 Regra Principal

```antlr
programa: (declaracaoVariavel | regra | condicional | switchCase | atribuicao)+ EOF;
```

Um programa CSS# consiste em uma ou mais declarações de:
- Variáveis
- Regras CSS
- Estruturas condicionais
- Estruturas switch/case
- Atribuições

#### 3.1.2 Declaração de Variáveis

```antlr
declaracaoVariavel: VAR_DEF PALAVRA DOIS_PONTOS valor;
```

**Sintaxe**: `>> nomeVariavel: valor`

**Exemplo**:
```css
>>corPrimaria: #FF00FF
>>largura: 100px
>>tema: 'claro'
```

#### 3.1.3 Atribuição

```antlr
atribuicao: PALAVRA DOIS_PONTOS valor PONTO_VIRGULA;
```

**Sintaxe**: `nomeVariavel: novoValor;`

**Exemplo**:
```css
tema: 'escuro';
largura: 200px;
```

#### 3.1.4 Regras CSS

```antlr
regra: seletor ABRE_CHAVES propriedades FECHA_CHAVES;

seletor: HASHTAG PALAVRA     // #id
       | PONTO PALAVRA       // .classe
       | TAG                 // div, body, etc
       | PALAVRA             // seletor customizado
       ;

propriedades: (ATRIBUTOS DOIS_PONTOS valores PONTO_VIRGULA)*;

valores: valor+;
```

**Exemplos**:
```css
body {
    background-color: #FFFFFF;
    margin: 10px;
}

#container {
    width: 500px;
    height: 300px;
}

.titulo {
    color: #FF0000;
    font-size: 24px;
}
```

#### 3.1.5 Valores e Medidas

```antlr
valor: STRING | PALAVRA | DIGITO | DIGITO_REAL | expressao | cor | medida;

medida: DIGITO_REAL UNIDADE_MEDIDA
      | DIGITO UNIDADE_MEDIDA
      | DIGITO
      ;

cor: PALAVRA_CHAVE ABRE_PAR DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA DIGITO_REAL FECHA_PAR  // rgb(255,255,255)
   | PALAVRA_CHAVE ABRE_PAR DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA DIGITO_REAL VIRGULA ALPHA_VALUE FECHA_PAR  // rgba(...)
   | HEX;  // #FF00FF
```

#### 3.1.6 Estruturas Condicionais

```antlr
condicional: PALAVRA_CHAVE ifStatement elseifStatement* elseStatement? PALAVRA_CHAVE;

ifStatement: (expressaoLogica | PALAVRA) DOIS_PONTOS regra;
elseifStatement: PALAVRA_CHAVE ifStatement;
elseStatement: PALAVRA_CHAVE DOIS_PONTOS regra;

expressaoLogica: (PALAVRA | STRING | DIGITO | DIGITO_REAL) OP_LOG (PALAVRA | STRING | DIGITO | DIGITO_REAL);
```

**Exemplo**:
```css
%if tema == 'escuro':
    body {
        background-color: #000000;
        color: #FFFFFF;
    }
%elseif tema == 'claro':
    body {
        background-color: #FFFFFF;
        color: #000000;
    }
%else:
    body {
        background-color: #CCCCCC;
    }
%endif
```

#### 3.1.7 Estrutura Switch/Case

```antlr
switchCase: PALAVRA_CHAVE PALAVRA DOIS_PONTOS caseStatement+ defaultStatement? PALAVRA_CHAVE;

caseStatement: PALAVRA_CHAVE PALAVRA DOIS_PONTOS regra;
defaultStatement: PALAVRA_CHAVE DOIS_PONTOS regra;
```

**Exemplo**:
```css
%switch tema:
    %case escuro: .titulo { color: #00FF00; }
    %case claro: .titulo { color: #0000FF; }
    %default: .titulo { color: #FF0000; }
%endswitch
```

#### 3.1.8 Expressões

```antlr
expressao: expressaoAritmetica | expressaoLogica;

expressaoAritmetica: termo ((OP_ARIT) termo)*;
termo: fator (OP_PRIMARIO fator)*;
fator: DIGITO | DIGITO_REAL | PALAVRA | ABRE_PAR expressaoAritmetica FECHA_PAR;
```

**Exemplos**:
```css
width: 100px + 50px;
height: largura * 2;
margin: espacamento / 2;
```

---

## 4. Analisador Semântico

### 4.1 `TabelaDeSimbolos.java`

Gerencia o armazenamento e recuperação de variáveis declaradas.

#### 4.1.1 Enum `TipoCSSSharp`

Define o sistema de tipos do CSS#:
- `NUMERO` - Valores numéricos puros
- `COR` - Cores (hex, rgb, rgba)
- `STRING` - Strings entre aspas simples
- `MEDIDA` - Números com unidades (px, vh, etc)
- `PALAVRA_CHAVE` - Palavras-chave CSS (solid, bold, etc)
- `INVALIDO` - Tipo sentinela para erros

#### 4.1.2 Classe Interna `EntradaTabelaSimbolos`

Armazena informações de cada variável:
- `nome` - Nome da variável
- `tipo` - Tipo da variável (TipoCSSSharp)
- `valor` - Valor textual armazenado

#### 4.1.3 Métodos Principais

| Método | Descrição |
|--------|-----------|
| `adicionar(String, TipoCSSSharp, String)` | Adiciona/atualiza variável na tabela |
| `existe(String)` | Verifica se variável foi declarada |
| `verificar(String)` | Retorna o tipo da variável |
| `obterValor(String)` | Retorna o valor armazenado |

**Usado em**: `CSSSharpSemantico`, `CSSSharpGeradorCSS`, `CSSSharpSemanticoUtils`

---

### 4.2 `CSSSharpSemanticoUtils.java`

Fornece utilitários para validação semântica.

#### 4.2.1 Coleção de Erros

```java
public static List<String> errosSemanticos
```
Lista centralizada que acumula todos os erros encontrados durante a análise.

#### 4.2.2 Conjuntos de Validação

- `PROPRIEDADES_VALIDAS` - Set com 19 propriedades CSS suportadas
- `PALAVRAS_CHAVE_CSS` - Set com palavras-chave CSS válidas (solid, bold, italic, etc)
- `TAGS_VALIDAS` - Set com 42 tags HTML5 válidas

#### 4.2.3 Métodos de Validação

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `adicionarErroSemantico(Token, String)` | void | Adiciona erro com localização |
| `propriedadeValida(String)` | boolean | Verifica se propriedade CSS é válida |
| `palavraChaveValida(String)` | boolean | Verifica se palavra-chave CSS é válida |
| `tagValida(String)` | boolean | Verifica se tag HTML é válida |
| `verificarTipoValor(ValorContext, TabelaDeSimbolos)` | TipoCSSSharp | Determina tipo de um valor |
| `verificarTipoExpressao(ExpressaoContext, TabelaDeSimbolos)` | TipoCSSSharp | Determina tipo de expressão |
| `verificarTipoExpressaoAritmetica(...)` | TipoCSSSharp | Valida expressão aritmética |
| `verificarTipoTermo(...)` | TipoCSSSharp | Valida termo aritmético |
| `verificarTipoFator(...)` | TipoCSSSharp | Valida fator (base recursão) |
| `verificarCor(CorContext)` | boolean | Valida formato de cor |

**Usado em**: `CSSSharpSemantico`, `CSSSharpGeradorCSS`

---

### 4.3 `CSSSharpSemantico.java`

Visitor que realiza a **primeira passada** do compilador, validando a semântica do código.

**Extends**: `CssSharpBaseVisitor<Void>`

#### 4.3.1 Atributos

- `TabelaDeSimbolos tabela` - Instância da tabela de símbolos

#### 4.3.2 Métodos Visit Principais

| Método | Validações Realizadas |
|--------|----------------------|
| `visitPrograma` | Inicializa nova tabela de símbolos |
| `visitDeclaracaoVariavel` | ✓ Verifica declaração duplicada<br>✓ Valida tipo do valor<br>✓ Adiciona à tabela de símbolos |
| `visitAtribuicao` | ✓ Verifica se variável foi declarada<br>✓ Valida compatibilidade de tipos |
| `visitRegra` | ✓ Valida seletor<br>✓ Valida propriedades |
| `visitSeletor` | ✓ Verifica se tag HTML é válida |
| `visitPropriedades` | ✓ Verifica propriedades CSS válidas<br>✓ Valida tipos dos valores |
| `visitCondicional` | ✓ Valida estrutura if/elseif/else |
| `visitIfStatement` | ✓ Valida expressão lógica ou variável<br>✓ Valida regra CSS interna |
| `visitExpressaoLogica` | ✓ Verifica variáveis declaradas nos operandos |
| `visitSwitchCase` | ✓ Verifica variável do switch declarada<br>✓ Valida cada case e default |
| `visitCor` | ✓ Valida formato de cor (hex, rgb, rgba) |

#### 4.3.3 Erros Detectados

- Variável já declarada anteriormente
- Variável não declarada
- Valor inválido para variável
- Tipo incompatível em atribuição
- Tag HTML inválida
- Propriedade CSS inválida ou não suportada
- Valor inválido para propriedade
- Variável usada em condição não declarada
- Formato de cor inválido

**Usado em**: `Principal.java` (primeira passada do compilador)

---

## 5. Geração de Código

### 5.1 `CSSSharpGeradorCSS.java`

Visitor que realiza a **segunda passada** do compilador, gerando código CSS válido.

**Extends**: `CssSharpBaseVisitor<Void>`

#### 5.1.1 Atributos

- `StringBuilder saida` - Acumulador de código CSS gerado
- `TabelaDeSimbolos tabela` - Tabela de símbolos (populada na primeira passada)
- `int nivelIndentacao` - Controla indentação do CSS
- `String INDENTACAO` - Constante com 4 espaços

#### 5.1.2 Métodos Visit Principais

| Método | Ação |
|--------|------|
| `visitPrograma` | Gera cabeçalho, processa variáveis e regras |
| `visitDeclaracaoVariavel` | Adiciona à tabela e gera comentário CSS |
| `visitAtribuicao` | Atualiza valor na tabela e gera comentário |
| `visitRegra` | Gera seletor CSS e bloco de propriedades |
| `visitCondicional` | Avalia condição e gera CSS do ramo verdadeiro |
| `visitSwitchCase` | Avalia variável e gera CSS do case correspondente |

#### 5.1.3 Métodos Auxiliares de Geração

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `gerarSeletor(SeletorContext)` | String | Gera seletor CSS (#id, .classe, tag) |
| `gerarPropriedades(PropriedadesContext)` | void | Gera bloco de propriedades CSS |
| `obterValorTexto(ValorContext)` | String | Extrai valor textual (substitui variáveis) |
| `gerarMedida(MedidaContext)` | String | Gera valor com unidade (100px, 2rem) |
| `gerarCor(CorContext)` | String | Gera cor (hex, rgb, rgba) |
| `gerarExpressao(ExpressaoContext)` | String | Gera expressão avaliada |
| `gerarExpressaoAritmetica(...)` | String | Gera expressão aritmética (a + b * c) |
| `gerarTermo(TermoContext)` | String | Gera termo (a * b / c) |
| `gerarFator(FatorContext)` | String | Gera fator (número, variável, parênteses) |
| `gerarExpressaoLogica(...)` | String | Gera expressão lógica como string |

#### 5.1.4 Métodos de Avaliação

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `avaliarCondicao(IfStatementContext)` | boolean | Avalia condição if/elseif |
| `avaliarExpressaoLogica(ExpressaoLogicaContext)` | boolean | Avalia expressão lógica (==, !=, >, <, etc) |
| `obterOperandoLogico(ExpressaoLogicaContext, int)` | String | Extrai operando da expressão (substitui variáveis) |
| `compararNumericamente(String, String)` | int | Compara valores numéricos |

#### 5.1.5 Substituição de Variáveis

O gerador substitui automaticamente referências a variáveis pelos seus valores:

**Entrada**:
```css
>>largura: 100px
>>cor: #FF0000

#box {
    width: largura;
    background-color: cor;
}
```

**Saída**:
```css
#box {
    width: 100px;
    background-color: #FF0000;
}
```

#### 5.1.6 Avaliação de Condicionais

O gerador avalia condições em tempo de compilação:

**Entrada**:
```css
>>tema: 'escuro'

%if tema == 'escuro':
    body { background-color: #000000; }
%elseif tema == 'claro':
    body { background-color: #FFFFFF; }
%else:
    body { background-color: #CCCCCC; }
%endif
```

**Saída**:
```css
/* Condicional %if */
body {
    background-color: #000000;
}
/* Fim condicional */
```

#### 5.1.7 Avaliação de Switch/Case

**Entrada**:
```css
>>tema: 'claro'

%switch tema:
    %case escuro: .titulo { color: #00FF00; }
    %case claro: .titulo { color: #0000FF; }
    %default: .titulo { color: #FF0000; }
%endswitch
```

**Saída**:
```css
/* Switch case */
.titulo {
    color: #0000FF;
}
/* Fim switch */
```

## 6. Como Usar

### 6.1 Pré-requisitos

- Java 11 ou superior
- Maven 3.6+
- ANTLR 4.7.2 (gerenciado pelo Maven)

### 6.2 Compilação do Projeto

```bash
# Limpar projeto
mvn clean

# Compilar (gera classes ANTLR e compila Java)
mvn compile

# Empacotar em JAR executável
mvn package
```

O JAR será gerado em: `target/CssSharp-1.0-SNAPSHOT-jar-with-dependencies.jar`

### 6.3 Execução

**Usando JAR**:
```bash
java -jar target/CssSharp-1.0-SNAPSHOT-jar-with-dependencies.jar <arquivo-entrada.csss> <arquivo-saida.css>
```

**Usando classpath**:
```bash
java -cp target/classes br.edu.ifsp.pep.csss.Principal <arquivo-entrada.csss> <arquivo-saida.css>
```

**Exemplo**:
```bash
java -cp target/classes br.edu.ifsp.pep.csss.Principal src/main/java/file/teste.csss saida.css
```

### 6.4 Saídas Possíveis

**Compilação Bem-sucedida**:
```
Compilação concluída com sucesso!
Arquivo CSS gerado: saida.css
```

**Erros Semânticos Detectados**:
```
Erros semânticos encontrados:
Linha 5:12 - variável 'corSecundaria' não foi declarada
Linha 8:5 - propriedade CSS 'tamanho-invalido' não é válida ou não é suportada
Linha 10:21 - variável 'espacamento' não foi declarada

Compilação abortada devido a erros semânticos.
```

---

## 7. Exemplos

### 7.1 Exemplo Completo com Todos os Recursos

**Arquivo**: `exemplo_completo.csss`

```css
// Declaração de variáveis
>>corPrimaria: #FF00FF
>>corSecundaria: #00FFFF
>>larguraPadrao: 200px
>>tema: 'escuro'
>>tamanhoFonte: 16px

// Condicional: Define cores do body baseado no tema
%if tema == 'escuro':
    body {
        background-color: #000000;
        color: #FFFFFF;
    }
%elseif tema == 'claro':
    body {
        background-color: #FFFFFF;
        color: #000000;
    }
%else:
    body {
        background-color: #CCCCCC;
        color: #333333;
    }
%endif

// Regras CSS normais
#container {
    width: 500px;
    height: 300px;
    color: rgb(255, 255, 255);
    margin: 10px;
    padding: 20px;
}

// Uso de variáveis
.box {
    width: larguraPadrao;
    background-color: corPrimaria;
    border: 2px solid corSecundaria;
}

// Seletores diversos
div {
    color: #FFFFFF;
    border: 1px solid #000000;
}

h1 {
    font-size: tamanhoFonte;
    font-weight: bold;
}

// Switch/case para estilo de títulos
%switch tema:
    %case escuro: .titulo { color: #00FF00; }
    %case claro: .titulo { color: #0000FF; }
    %default: .titulo { color: #FF0000; }
%endswitch

// Uso de rgba
.transparente {
    background-color: rgba(255, 0, 0, 0.5);
}
```

**Saída**: `exemplo_completo.css`

```css
/* CSS gerado automaticamente pelo compilador CSS# */

/* Variável: corPrimaria = #FF00FF */
/* Variável: corSecundaria = #00FFFF */
/* Variável: larguraPadrao = 200px */
/* Variável: tema = escuro */
/* Variável: tamanhoFonte = 16px */
/* Condicional %if */
body {
    background-color: #000000;
    color: #FFFFFF;
}

/* Fim condicional */

#container {
    width: 500px;
    height: 300px;
    color: rgb(255, 255, 255);
    margin: 10px;
    padding: 20px;
}

.box {
    width: 200px;
    background-color: #FF00FF;
    border: 2px solid #00FFFF;
}

div {
    color: #FFFFFF;
    border: 1px solid #000000;
}

h1 {
    font-size: 16px;
    font-weight: bold;
}

/* Switch case */
.titulo {
    color: #00FF00;
}

/* Fim switch */

.transparente {
    background-color: rgba(255, 0, 0, 0.5);
}
```

### 7.2 Arquivos de Teste com Erros

O projeto inclui 4 arquivos de teste que **devem gerar erros**:

#### 7.2.1 `teste_erro_variavel.csss`
Testa detecção de variáveis não declaradas.

#### 7.2.2 `teste_erro_tipo.csss`
Testa detecção de tipos incompatíveis.

#### 7.2.3 `teste_erro_duplicado.csss`
Testa detecção de declarações duplicadas.

#### 7.2.4 `teste_erro_condicional.csss`
Testa erros em estruturas condicionais e tags inválidas.

---

## 8. Autores e Licença

**Desenvolvido por**: João Pedro Franca, Leo Martelli, Maristela Ayumi, Welinton Fernando  
**Instituição**: IFSP - Instituto Federal de São Paulo  
**Curso**: Bacharelado em Ciência da Computação  
**Disciplina**: PEPCPLD - Compiladores

**Licença**: MIT (ver arquivo `LICENSE`)

---
