package br.edu.ifsp.pep.csss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import br.edu.ifsp.pep.csss.TabelaDeSimbolos.TipoCSSSharp;
import br.edu.ifsp.pep.lexico.CssSharpParser;

public class CSSSharpSemanticoUtils {
    
    public static List<String> errosSemanticos = new ArrayList<>();
    
    // Propriedades CSS válidas suportadas
    private static final Set<String> PROPRIEDADES_VALIDAS = new HashSet<>(Arrays.asList(
        "top", "left", "width", "height", "font-size", "margin", "padding",
        "color", "background-color", "border", "display", "position",
        "float", "clear", "z-index", "overflow", "text-align",
        "font-weight", "font-style", "text-decoration"
    ));
    
    // Palavras-chave CSS válidas
    private static final Set<String> PALAVRAS_CHAVE_CSS = new HashSet<>(Arrays.asList(
        "solid", "dashed", "dotted", "double", "none", "hidden",
        "bold", "normal", "italic", "underline", "overline", "line-through",
        "block", "inline", "inline-block", "flex", "grid", "none",
        "absolute", "relative", "fixed", "static", "sticky",
        "left", "right", "center", "justify", "auto", "transparent"
    ));
    
    // Tags HTML válidas
    private static final Set<String> TAGS_VALIDAS = new HashSet<>(Arrays.asList(
        "html", "head", "title", "body", "header", "footer", "nav",
        "section", "article", "aside", "main", "h1", "h2", "h3",
        "h4", "h5", "h6", "p", "b", "i", "strong", "em", "br",
        "blockquote", "code", "ul", "ol", "li", "a", "img", "figure",
        "figcaption", "table", "tr", "td", "th", "thead", "tbody",
        "tfoot", "form", "input", "textarea", "button", "select",
        "option", "label", "style", "link", "script", "div", "span", "iframe"
    ));
    
    public static void adicionarErroSemantico(Token t, String mensagem) {
        int linha = t.getLine();
        int coluna = t.getCharPositionInLine() + 1;
        errosSemanticos.add(String.format("Linha %d:%d - %s", linha, coluna, mensagem));
    }
    
    public static boolean propriedadeValida(String propriedade) {
        return PROPRIEDADES_VALIDAS.contains(propriedade);
    }
    
    public static boolean palavraChaveValida(String palavra) {
        return PALAVRAS_CHAVE_CSS.contains(palavra);
    }
    
    public static boolean tagValida(String tag) {
        return TAGS_VALIDAS.contains(tag);
    }
    
    // Verifica se é um valor numérico válido
    public static TipoCSSSharp verificarTipoValor(CssSharpParser.ValorContext ctx, TabelaDeSimbolos tabela) {
        if (ctx.DIGITO() != null || ctx.DIGITO_REAL() != null) {
            return TipoCSSSharp.NUMERO;
        } else if (ctx.STRING() != null) {
            return TipoCSSSharp.STRING;
        } else if (ctx.medida() != null) {
            return TipoCSSSharp.MEDIDA;
        } else if (ctx.cor() != null) {
            return TipoCSSSharp.COR;
        } else if (ctx.PALAVRA() != null) {
            String palavra = ctx.PALAVRA().getText();
            // Verifica se é uma variável
            if (tabela.existe(palavra)) {
                return tabela.verificar(palavra);
            }
            // Verifica se é uma palavra-chave CSS válida
            if (palavraChaveValida(palavra)) {
                return TipoCSSSharp.PALAVRA_CHAVE;
            }
            return TipoCSSSharp.INVALIDO;
        } else if (ctx.expressao() != null) {
            return verificarTipoExpressao(ctx.expressao(), tabela);
        }
        return TipoCSSSharp.INVALIDO;
    }
    
    // Verifica tipo de expressão (aritmética ou lógica)
    public static TipoCSSSharp verificarTipoExpressao(CssSharpParser.ExpressaoContext ctx, TabelaDeSimbolos tabela) {
        if (ctx.expressaoAritmetica() != null) {
            return verificarTipoExpressaoAritmetica(ctx.expressaoAritmetica(), tabela);
        } else if (ctx.expressaoLogica() != null) {
            // Expressões lógicas retornam booleano, mas no contexto CSS não são valores válidos
            return TipoCSSSharp.INVALIDO;
        }
        return TipoCSSSharp.INVALIDO;
    }
    
    // Verifica tipo de expressão aritmética
    public static TipoCSSSharp verificarTipoExpressaoAritmetica(CssSharpParser.ExpressaoAritmeticaContext ctx, TabelaDeSimbolos tabela) {
        TipoCSSSharp tipoResultado = TipoCSSSharp.NUMERO;
        
        for (CssSharpParser.TermoContext termo : ctx.termo()) {
            TipoCSSSharp tipoTermo = verificarTipoTermo(termo, tabela);
            if (tipoTermo == TipoCSSSharp.INVALIDO) {
                return TipoCSSSharp.INVALIDO;
            }
            // Se algum termo for medida, o resultado é medida
            if (tipoTermo == TipoCSSSharp.MEDIDA) {
                tipoResultado = TipoCSSSharp.MEDIDA;
            }
        }
        
        return tipoResultado;
    }
    
    // Verifica tipo de termo
    public static TipoCSSSharp verificarTipoTermo(CssSharpParser.TermoContext ctx, TabelaDeSimbolos tabela) {
        TipoCSSSharp tipoResultado = TipoCSSSharp.NUMERO;
        
        for (CssSharpParser.FatorContext fator : ctx.fator()) {
            TipoCSSSharp tipoFator = verificarTipoFator(fator, tabela);
            if (tipoFator == TipoCSSSharp.INVALIDO) {
                return TipoCSSSharp.INVALIDO;
            }
            // Se algum fator for medida, o resultado é medida
            if (tipoFator == TipoCSSSharp.MEDIDA) {
                tipoResultado = TipoCSSSharp.MEDIDA;
            }
        }
        
        return tipoResultado;
    }
    
    // Verifica tipo de fator
    public static TipoCSSSharp verificarTipoFator(CssSharpParser.FatorContext ctx, TabelaDeSimbolos tabela) {
        if (ctx.DIGITO() != null || ctx.DIGITO_REAL() != null) {
            return TipoCSSSharp.NUMERO;
        } else if (ctx.PALAVRA() != null) {
            String nome = ctx.PALAVRA().getText();
            if (tabela.existe(nome)) {
                return tabela.verificar(nome);
            } else {
                adicionarErroSemantico(ctx.PALAVRA().getSymbol(), 
                    "variável '" + nome + "' não foi declarada");
                return TipoCSSSharp.INVALIDO;
            }
        } else if (ctx.expressaoAritmetica() != null) {
            return verificarTipoExpressaoAritmetica(ctx.expressaoAritmetica(), tabela);
        }
        return TipoCSSSharp.INVALIDO;
    }
    
    // Verifica se a cor está no formato válido
    public static boolean verificarCor(CssSharpParser.CorContext ctx) {
        if (ctx.HEX() != null) {
            // Cor hexadecimal já validada pelo lexer
            return true;
        } else if (ctx.PALAVRA_CHAVE() != null) {
            String funcao = ctx.PALAVRA_CHAVE().getText();
            return funcao.equals("rgb") || funcao.equals("rgba");
        }
        return false;
    }
}
