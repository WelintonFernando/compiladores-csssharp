package br.edu.ifsp.pep.csss;

import br.edu.ifsp.pep.csss.TabelaDeSimbolos.TipoCSSSharp;
import br.edu.ifsp.pep.lexico.CssSharpBaseVisitor;
import br.edu.ifsp.pep.lexico.CssSharpParser;

public class CSSSharpGeradorCSS extends CssSharpBaseVisitor<Void> {
    
    private StringBuilder saida;
    private TabelaDeSimbolos tabela;
    private int nivelIndentacao;
    private static final String INDENTACAO = "    "; // 4 espaços
    
    public CSSSharpGeradorCSS() {
        this.saida = new StringBuilder();
        this.nivelIndentacao = 0;
    }
    
    @Override
    public Void visitPrograma(CssSharpParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        
        // Adiciona comentário de cabeçalho
        saida.append("/* CSS gerado automaticamente pelo compilador CSS# */\n\n");
        
        // Primeira passada: processa apenas declarações de variáveis
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof CssSharpParser.DeclaracaoVariavelContext) {
                visitDeclaracaoVariavel((CssSharpParser.DeclaracaoVariavelContext) ctx.getChild(i));
            }
        }
        
        // Segunda passada: processa regras CSS, condicionais, switches e atribuições
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof CssSharpParser.RegraContext) {
                visitRegra((CssSharpParser.RegraContext) ctx.getChild(i));
            } else if (ctx.getChild(i) instanceof CssSharpParser.CondicionalContext) {
                visitCondicional((CssSharpParser.CondicionalContext) ctx.getChild(i));
            } else if (ctx.getChild(i) instanceof CssSharpParser.SwitchCaseContext) {
                visitSwitchCase((CssSharpParser.SwitchCaseContext) ctx.getChild(i));
            } else if (ctx.getChild(i) instanceof CssSharpParser.AtribuicaoContext) {
                visitAtribuicao((CssSharpParser.AtribuicaoContext) ctx.getChild(i));
            }
        }
        
        return null;
    }
    
    @Override
    public Void visitDeclaracaoVariavel(CssSharpParser.DeclaracaoVariavelContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        String valor = obterValorTexto(ctx.valor());
        
        // Determina o tipo do valor
        TipoCSSSharp tipo = CSSSharpSemanticoUtils.verificarTipoValor(ctx.valor(), tabela);
        
        // Adiciona à tabela de símbolos
        tabela.adicionar(nomeVar, tipo, valor);
        
        // CSS não tem variáveis nativas (exceto custom properties CSS3)
        // Podemos gerar como comentário ou usar CSS custom properties
        saida.append("/* Variável: ").append(nomeVar).append(" = ").append(valor).append(" */\n");
        
        return null;
    }
    
    @Override
    public Void visitAtribuicao(CssSharpParser.AtribuicaoContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        String valor = obterValorTexto(ctx.valor());
        
        // Atualiza o valor na tabela de símbolos
        if (tabela.existe(nomeVar)) {
            TipoCSSSharp tipo = tabela.verificar(nomeVar);
            tabela.adicionar(nomeVar, tipo, valor); // Sobrescreve
            
            saida.append("/* Atribuição: ").append(nomeVar).append(" = ").append(valor).append(" */\n");
        }
        
        return null;
    }
    
    @Override
    public Void visitRegra(CssSharpParser.RegraContext ctx) {
        // Gera o seletor CSS
        String seletor = gerarSeletor(ctx.seletor());
        saida.append(seletor).append(" {\n");
        
        nivelIndentacao++;
        
        // Gera as propriedades CSS
        if (ctx.propriedades() != null) {
            gerarPropriedades(ctx.propriedades());
        }
        
        nivelIndentacao--;
        
        saida.append("}\n\n");
        
        return null;
    }
    
    private String gerarSeletor(CssSharpParser.SeletorContext ctx) {
        StringBuilder sel = new StringBuilder();
        
        if (ctx.HASHTAG() != null) {
            // Seletor de ID: #id
            sel.append("#").append(ctx.PALAVRA().getText());
        } else if (ctx.PONTO() != null) {
            // Seletor de classe: .classe
            sel.append(".").append(ctx.PALAVRA().getText());
        } else if (ctx.TAG() != null) {
            // Seletor de tag: div, body, etc
            sel.append(ctx.TAG().getText());
        } else if (ctx.PALAVRA() != null) {
            // Seletor customizado (pode ser uma classe sem ponto)
            sel.append(".").append(ctx.PALAVRA().getText());
        }
        
        return sel.toString();
    }
    
    private void gerarPropriedades(CssSharpParser.PropriedadesContext ctx) {
        for (int i = 0; i < ctx.ATRIBUTOS().size(); i++) {
            String propriedade = ctx.ATRIBUTOS(i).getText();
            CssSharpParser.ValoresContext valores = ctx.valores(i);
            
            // Gera a propriedade CSS
            adicionarIndentacao();
            saida.append(propriedade).append(": ");
            
            // Gera os valores da propriedade
            if (valores != null) {
                boolean primeiro = true;
                for (CssSharpParser.ValorContext valor : valores.valor()) {
                    if (!primeiro) {
                        saida.append(" ");
                    }
                    String valorTexto = obterValorTexto(valor);
                    saida.append(valorTexto);
                    primeiro = false;
                }
            }
            
            saida.append(";\n");
        }
    }
    
    private String obterValorTexto(CssSharpParser.ValorContext ctx) {
        if (ctx.STRING() != null) {
            // Remove as aspas simples das strings
            String str = ctx.STRING().getText();
            return str.substring(1, str.length() - 1);
        } else if (ctx.PALAVRA() != null) {
            String palavra = ctx.PALAVRA().getText();
            // Verifica se é uma variável
            if (tabela.existe(palavra)) {
                return tabela.obterValor(palavra);
            }
            return palavra;
        } else if (ctx.DIGITO() != null) {
            return ctx.DIGITO().getText();
        } else if (ctx.DIGITO_REAL() != null) {
            return ctx.DIGITO_REAL().getText();
        } else if (ctx.medida() != null) {
            return gerarMedida(ctx.medida());
        } else if (ctx.cor() != null) {
            return gerarCor(ctx.cor());
        } else if (ctx.expressao() != null) {
            return gerarExpressao(ctx.expressao());
        }
        
        return ctx.getText();
    }
    
    private String gerarMedida(CssSharpParser.MedidaContext ctx) {
        StringBuilder medida = new StringBuilder();
        
        if (ctx.DIGITO_REAL() != null) {
            medida.append(ctx.DIGITO_REAL().getText());
        } else if (ctx.DIGITO() != null) {
            medida.append(ctx.DIGITO().getText());
        }
        
        if (ctx.UNIDADE_MEDIDA() != null) {
            medida.append(ctx.UNIDADE_MEDIDA().getText());
        }
        
        return medida.toString();
    }
    
    private String gerarCor(CssSharpParser.CorContext ctx) {
        if (ctx.HEX() != null) {
            return ctx.HEX().getText();
        } else if (ctx.PALAVRA_CHAVE() != null) {
            String funcao = ctx.PALAVRA_CHAVE().getText();
            StringBuilder cor = new StringBuilder(funcao).append("(");
            
            // RGB ou RGBA
            if (ctx.DIGITO_REAL().size() >= 3) {
                cor.append(ctx.DIGITO_REAL(0).getText()).append(", ");
                cor.append(ctx.DIGITO_REAL(1).getText()).append(", ");
                cor.append(ctx.DIGITO_REAL(2).getText());
                
                // Se for RGBA, adiciona o alpha
                if (ctx.ALPHA_VALUE() != null) {
                    cor.append(", ").append(ctx.ALPHA_VALUE().getText());
                }
            }
            
            cor.append(")");
            return cor.toString();
        }
        
        return ctx.getText();
    }
    
    private String gerarExpressao(CssSharpParser.ExpressaoContext ctx) {
        if (ctx.expressaoAritmetica() != null) {
            return gerarExpressaoAritmetica(ctx.expressaoAritmetica());
        } else if (ctx.expressaoLogica() != null) {
            return gerarExpressaoLogica(ctx.expressaoLogica());
        }
        return "";
    }
    
    private String gerarExpressaoAritmetica(CssSharpParser.ExpressaoAritmeticaContext ctx) {
        StringBuilder expr = new StringBuilder();
        
        // Primeiro termo
        expr.append(gerarTermo(ctx.termo(0)));
        
        // Operadores e termos subsequentes
        for (int i = 1; i < ctx.termo().size(); i++) {
            if (ctx.OP_ARIT(i - 1) != null) {
                expr.append(" ").append(ctx.OP_ARIT(i - 1).getText()).append(" ");
            }
            expr.append(gerarTermo(ctx.termo(i)));
        }
        
        return expr.toString();
    }
    
    private String gerarTermo(CssSharpParser.TermoContext ctx) {
        StringBuilder termo = new StringBuilder();
        
        // Primeiro fator
        termo.append(gerarFator(ctx.fator(0)));
        
        // Operadores e fatores subsequentes
        for (int i = 1; i < ctx.fator().size(); i++) {
            if (ctx.OP_PRIMARIO(i - 1) != null) {
                termo.append(" ").append(ctx.OP_PRIMARIO(i - 1).getText()).append(" ");
            }
            termo.append(gerarFator(ctx.fator(i)));
        }
        
        return termo.toString();
    }
    
    private String gerarFator(CssSharpParser.FatorContext ctx) {
        if (ctx.DIGITO() != null) {
            return ctx.DIGITO().getText();
        } else if (ctx.DIGITO_REAL() != null) {
            return ctx.DIGITO_REAL().getText();
        } else if (ctx.PALAVRA() != null) {
            String palavra = ctx.PALAVRA().getText();
            // Substitui por valor da variável se existir
            if (tabela.existe(palavra)) {
                return tabela.obterValor(palavra);
            }
            return palavra;
        } else if (ctx.expressaoAritmetica() != null) {
            return "(" + gerarExpressaoAritmetica(ctx.expressaoAritmetica()) + ")";
        }
        return "";
    }
    
    private String gerarExpressaoLogica(CssSharpParser.ExpressaoLogicaContext ctx) {
        StringBuilder expr = new StringBuilder();
        
        // Primeiro operando
        if (ctx.PALAVRA(0) != null) {
            String palavra = ctx.PALAVRA(0).getText();
            if (tabela.existe(palavra)) {
                expr.append(tabela.obterValor(palavra));
            } else {
                expr.append(palavra);
            }
        } else if (ctx.STRING(0) != null) {
            expr.append(ctx.STRING(0).getText());
        } else if (ctx.DIGITO(0) != null) {
            expr.append(ctx.DIGITO(0).getText());
        } else if (ctx.DIGITO_REAL(0) != null) {
            expr.append(ctx.DIGITO_REAL(0).getText());
        }
        
        // Operador lógico
        if (ctx.OP_LOG() != null) {
            expr.append(" ").append(ctx.OP_LOG().getText()).append(" ");
        }
        
        // Segundo operando
        if (ctx.PALAVRA(1) != null) {
            String palavra = ctx.PALAVRA(1).getText();
            if (tabela.existe(palavra)) {
                expr.append(tabela.obterValor(palavra));
            } else {
                expr.append(palavra);
            }
        } else if (ctx.STRING(1) != null) {
            expr.append(ctx.STRING(1).getText());
        } else if (ctx.DIGITO(1) != null) {
            expr.append(ctx.DIGITO(1).getText());
        } else if (ctx.DIGITO_REAL(1) != null) {
            expr.append(ctx.DIGITO_REAL(1).getText());
        }
        
        return expr.toString();
    }
    
    @Override
    public Void visitCondicional(CssSharpParser.CondicionalContext ctx) {
        saida.append("/* Condicional %if */\n");
        
        boolean algumIfExecutado = false;
        
        // Avalia o if principal
        if (ctx.ifStatement() != null && ctx.ifStatement().regra() != null) {
            boolean condicaoAtendida = avaliarCondicao(ctx.ifStatement());
            if (condicaoAtendida) {
                visitRegra(ctx.ifStatement().regra());
                algumIfExecutado = true;
            }
        }
        
        // Avalia os elseif (somente se o if não foi executado)
        if (!algumIfExecutado && ctx.elseifStatement() != null) {
            for (CssSharpParser.ElseifStatementContext elseifCtx : ctx.elseifStatement()) {
                if (elseifCtx.ifStatement() != null && elseifCtx.ifStatement().regra() != null) {
                    boolean condicaoAtendida = avaliarCondicao(elseifCtx.ifStatement());
                    if (condicaoAtendida) {
                        visitRegra(elseifCtx.ifStatement().regra());
                        algumIfExecutado = true;
                        break; // Para no primeiro elseif verdadeiro
                    }
                }
            }
        }
        
        // Avalia o else (somente se nenhum if/elseif foi executado)
        if (!algumIfExecutado && ctx.elseStatement() != null && ctx.elseStatement().regra() != null) {
            visitRegra(ctx.elseStatement().regra());
        }
        
        saida.append("/* Fim condicional */\n\n");
        
        return null;
    }
    
    private boolean avaliarCondicao(CssSharpParser.IfStatementContext ctx) {
        if (ctx.expressaoLogica() != null) {
            return avaliarExpressaoLogica(ctx.expressaoLogica());
        } else if (ctx.PALAVRA() != null) {
            String nomeVar = ctx.PALAVRA().getText();
            if (tabela.existe(nomeVar)) {
                String valor = tabela.obterValor(nomeVar);
                // Considera "true", valores não vazios e != "0" como verdadeiros
                return valor != null && !valor.isEmpty() && !valor.equals("0") && !valor.equalsIgnoreCase("false");
            }
        }
        return false;
    }
    
    private boolean avaliarExpressaoLogica(CssSharpParser.ExpressaoLogicaContext ctx) {
        String operando1 = obterOperandoLogico(ctx, 0);
        String operando2 = obterOperandoLogico(ctx, 1);
        String operador = ctx.OP_LOG().getText();
        
        // Debug: imprime os valores para verificação
        System.out.println("DEBUG: operando1='" + operando1 + "' operando2='" + operando2 + "' operador=" + operador);
        
        switch (operador) {
            case "==":
                return operando1.equals(operando2);
            case "!=":
                return !operando1.equals(operando2);
            case ">":
                return compararNumericamente(operando1, operando2) > 0;
            case "<":
                return compararNumericamente(operando1, operando2) < 0;
            case ">=":
                return compararNumericamente(operando1, operando2) >= 0;
            case "<=":
                return compararNumericamente(operando1, operando2) <= 0;
            default:
                return false;
        }
    }
    
    private String obterOperandoLogico(CssSharpParser.ExpressaoLogicaContext ctx, int indice) {
        // Precisamos pegar os operandos na ordem em que aparecem, não por tipo
        // A gramática é: (PALAVRA | STRING | DIGITO | DIGITO_REAL) OP_LOG (PALAVRA | STRING | DIGITO | DIGITO_REAL)
        
        // Primeiro operando (índice 0) é o child 0
        // Segundo operando (índice 1) é o child 2 (child 1 é o OP_LOG)
        int childIndex = (indice == 0) ? 0 : 2;
        
        if (childIndex >= ctx.getChildCount()) {
            return "";
        }
        
        String textoToken = ctx.getChild(childIndex).getText();
        
        // Se for STRING (tem aspas), remove as aspas
        if (textoToken.startsWith("'") && textoToken.endsWith("'")) {
            return textoToken.substring(1, textoToken.length() - 1);
        }
        
        // Se for PALAVRA, verifica se é variável
        if (ctx.getChild(childIndex) instanceof org.antlr.v4.runtime.tree.TerminalNode) {
            org.antlr.v4.runtime.tree.TerminalNode terminal = (org.antlr.v4.runtime.tree.TerminalNode) ctx.getChild(childIndex);
            if (terminal.getSymbol().getType() == CssSharpParser.PALAVRA) {
                String palavra = textoToken;
                if (tabela.existe(palavra)) {
                    return tabela.obterValor(palavra);
                }
                return palavra;
            }
        }
        
        // Para DIGITO ou DIGITO_REAL, retorna o texto direto
        return textoToken;
    }
    
    private int compararNumericamente(String a, String b) {
        try {
            double numA = Double.parseDouble(a);
            double numB = Double.parseDouble(b);
            return Double.compare(numA, numB);
        } catch (NumberFormatException e) {
            // Se não forem números, compara como strings
            return a.compareTo(b);
        }
    }
    
    @Override
    public Void visitSwitchCase(CssSharpParser.SwitchCaseContext ctx) {
        saida.append("/* Switch case */\n");
        
        String nomeVar = ctx.PALAVRA().getText();
        String valorVar = "";
        
        if (tabela.existe(nomeVar)) {
            valorVar = tabela.obterValor(nomeVar);
        }
        
        // Procura o case correspondente
        boolean caseEncontrado = false;
        for (CssSharpParser.CaseStatementContext caseStmt : ctx.caseStatement()) {
            String valorCase = caseStmt.PALAVRA().getText();
            if (valorVar.equals(valorCase)) {
                visitRegra(caseStmt.regra());
                caseEncontrado = true;
                break;
            }
        }
        
        // Se nenhum case foi encontrado, usa o default
        if (!caseEncontrado && ctx.defaultStatement() != null) {
            visitRegra(ctx.defaultStatement().regra());
        }
        
        saida.append("/* Fim switch */\n\n");
        
        return null;
    }
    
    private void adicionarIndentacao() {
        for (int i = 0; i < nivelIndentacao; i++) {
            saida.append(INDENTACAO);
        }
    }
    
    public String getCodigoGerado() {
        return saida.toString();
    }
}
