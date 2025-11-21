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

        // processa primeiro as declarações de variáveis
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof CssSharpParser.DeclaracaoVariavelContext) {
                visitDeclaracaoVariavel((CssSharpParser.DeclaracaoVariavelContext) ctx.getChild(i));
            }
        }
        
        // processa as regras do CSS, condicionais, switchs e zas e zas
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
        
        // verifica o tipo
        TipoCSSSharp tipo = CSSSharpSemanticoUtils.verificarTipoValor(ctx.valor(), tabela);
        tabela.adicionar(nomeVar, tipo, valor);

        //saida.append("/* era variavel: ").append(nomeVar).append(" = ").append(valor).append(" */\n");

        return null;
    }
    
    @Override
    public Void visitAtribuicao(CssSharpParser.AtribuicaoContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        String valor = obterValorTexto(ctx.valor());
        
        // atualiza o valor na tabela de simbolos
        if (tabela.existe(nomeVar)) {
            TipoCSSSharp tipo = tabela.verificar(nomeVar);
            tabela.adicionar(nomeVar, tipo, valor); // Sobrescreve
            
//            saida.append("/* Atribuição: ").append(nomeVar).append(" = ").append(valor).append(" */\n");
        }
        
        return null;
    }
    
    @Override
    public Void visitRegra(CssSharpParser.RegraContext ctx) {
        // gera o seletor CSS
        String seletor = gerarSeletor(ctx.seletor());
        saida.append(seletor).append(" {\n");
        
        nivelIndentacao++;
        
        // gera as propriedades
        if (ctx.propriedades() != null) {
            gerarPropriedades(ctx.propriedades());
        }
        
        nivelIndentacao--;
        
        saida.append("}\n\n");
        
        return null;
    }
    
    private String gerarSeletor(CssSharpParser.SeletorContext ctx) {
        StringBuilder sel = new StringBuilder();
        
        if (ctx.HASHTAG() != null) { //#id
            sel.append("#").append(ctx.PALAVRA().getText());
        } else if (ctx.PONTO() != null) { //.class
            sel.append(".").append(ctx.PALAVRA().getText());
        } else if (ctx.TAG() != null) { // tag
            sel.append(ctx.TAG().getText());
        } else if (ctx.PALAVRA() != null) { //palavra
            sel.append(".").append(ctx.PALAVRA().getText());
        }
        
        return sel.toString();
    }
    
    private void gerarPropriedades(CssSharpParser.PropriedadesContext ctx) {
        for (int i = 0; i < ctx.ATRIBUTOS().size(); i++) {
            String propriedade = ctx.ATRIBUTOS(i).getText();
            CssSharpParser.ValoresContext valores = ctx.valores(i);
            
            // gera as propriedade CSS
            adicionarIndentacao();
            saida.append(propriedade).append(": ");
            
            // gera os valores das propriedades
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
            // remove as aspas das strings
            String str = ctx.STRING().getText();
            return str.substring(1, str.length() - 1);
        } else if (ctx.PALAVRA() != null) {
            String palavra = ctx.PALAVRA().getText();
            // verifica se é uma variável
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
        
        // gera o pirmeiro termo
        expr.append(gerarTermo(ctx.termo(0)));
        
        // faz os termos da frente
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
        
        // primeiro fator
        termo.append(gerarFator(ctx.fator(0)));
        
        // opradores e fatores subsequentes
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
            // troca a variável pelo valor, se ela existir
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
        
        // primeiro operando
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
        
        // op lógico
        if (ctx.OP_LOG() != null) {
            expr.append(" ").append(ctx.OP_LOG().getText()).append(" ");
        }
        
        // segundo operando
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
//        saida.append("/* Condicional %if */\n");
        
        boolean algumIfExecutado = false;
        
        // avalia o if principal
        if (ctx.ifStatement() != null && ctx.ifStatement().regra() != null) {
            boolean condicaoAtendida = avaliarCondicao(ctx.ifStatement());
            if (condicaoAtendida) {
                visitRegra(ctx.ifStatement().regra());
                algumIfExecutado = true;
            }
        }
        
        // ve elseif (só se o if não foi executado)
        if (!algumIfExecutado && ctx.elseifStatement() != null) {
            for (CssSharpParser.ElseifStatementContext elseifCtx : ctx.elseifStatement()) {
                if (elseifCtx.ifStatement() != null && elseifCtx.ifStatement().regra() != null) {
                    boolean condicaoAtendida = avaliarCondicao(elseifCtx.ifStatement());
                    if (condicaoAtendida) {
                        visitRegra(elseifCtx.ifStatement().regra());
                        algumIfExecutado = true;
                        break; // quebra assim que o primeiro elseif verdadeiro for encontrado
                    }
                }
            }
        }

        // else
        if (!algumIfExecutado && ctx.elseStatement() != null && ctx.elseStatement().regra() != null) {
            visitRegra(ctx.elseStatement().regra());
        }
        
//        saida.append("/* Fim condicional */\n\n");
        
        return null;
    }
    
    private boolean avaliarCondicao(CssSharpParser.IfStatementContext ctx) {
        if (ctx.expressaoLogica() != null) {
            return avaliarExpressaoLogica(ctx.expressaoLogica());
        } else if (ctx.PALAVRA() != null) {
            String nomeVar = ctx.PALAVRA().getText();
            if (tabela.existe(nomeVar)) {
                String valor = tabela.obterValor(nomeVar);
                // "true" para valores não vazios e != "0" como verdadeiros
                return valor != null && !valor.isEmpty() && !valor.equals("0") && !valor.equalsIgnoreCase("false");
            }
        }
        return false;
    }
    
    private boolean avaliarExpressaoLogica(CssSharpParser.ExpressaoLogicaContext ctx) {
        String operando1 = obterOperandoLogico(ctx, 0);
        String operando2 = obterOperandoLogico(ctx, 1);
        String operador = ctx.OP_LOG().getText();
        
//        System.out.println("DEBUG: operando1='" + operando1 + "' operando2='" + operando2 + "' operador=" + operador);
        
        switch (operador) {
            // %if OPERANDO1 (==, !=, >, <, >=, <=) OPERANDO2 %endif

            case "==":
                return operando1.equals(operando2); // SE OPERANDO1 IGUAL OPERANDO2
            case "!=":
                return !operando1.equals(operando2); // SE OPERANDO1 *NÃO* IGUAL OPERANDO2
            case ">":
                return compararNumericamente(operando1, operando2) > 0; // SE OPERANDO1 MAIOR QUE OPERANDO2
            case "<":
                return compararNumericamente(operando1, operando2) < 0; // SE OPERANDO1 MENOR QUE OPERANDO2
            case ">=":
                return compararNumericamente(operando1, operando2) >= 0; // SE OPERANDO1 MAIOR IGUAL OPERANDO2
            case "<=":
                return compararNumericamente(operando1, operando2) <= 0; // SE OPERANDO1 MENOR IGUAL OPERANDO2
            default:
                return false;
        }
    }
    
    private String obterOperandoLogico(CssSharpParser.ExpressaoLogicaContext ctx, int indice) {

        // A gramática é: (PALAVRA | STRING | DIGITO | DIGITO_REAL) OP_LOG (PALAVRA | STRING | DIGITO | DIGITO_REAL)
        //         child 0 ^                                child 1 ^  child 2 ^

        int childIndex;

        if (indice == 0) {
            childIndex = 0;
        }else {
            childIndex = 2;
        }

        
        if (childIndex >= ctx.getChildCount()) {
            return "";
        }
        
        String textoToken = ctx.getChild(childIndex).getText();
        
        // se for STRING (tem aspas), remove as aspas
        if (textoToken.startsWith("'") && textoToken.endsWith("'")) {
            return textoToken.substring(1, textoToken.length() - 1);
        }
        
        // se for PALAVRA ele vai ver se é uma variavel
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
        
        // se não se encaixar em nenhuma delas, vai ser um digto, ai retorna direto
        return textoToken;
    }

    private int compararNumericamente(String a, String b) {
        try {
            double numA = Double.parseDouble(a);
            double numB = Double.parseDouble(b);
            return Double.compare(numA, numB);
        } catch (NumberFormatException e) { // pode ser que não seja numeros, ai vai ter que comparar como string
            return a.compareTo(b);
        }
    }
    
    @Override
    public Void visitSwitchCase(CssSharpParser.SwitchCaseContext ctx) {
//        saida.append("/* Switch case */\n");
        
        String nomeVar = ctx.PALAVRA().getText();
        String valorVar = "";
        
        if (tabela.existe(nomeVar)) {
            valorVar = tabela.obterValor(nomeVar);
        }
        
        // procura o case que bate
        boolean caseEncontrado = false;
        for (CssSharpParser.CaseStatementContext caseStmt : ctx.caseStatement()) {
            String valorCase = caseStmt.PALAVRA().getText();
            if (valorVar.equals(valorCase)) {
                visitRegra(caseStmt.regra());
                caseEncontrado = true;
                break;
            }
        }
        
        // se não entrar em nada vai no default
        if (!caseEncontrado && ctx.defaultStatement() != null) {
            visitRegra(ctx.defaultStatement().regra());
        }
        
//        saida.append("/* Fim switch */\n\n");
        
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

    public String olaDanilo() {
        return "passa a gente nessa bomba 😭🙏";
    }
}

/*
  Código nosso que estás em C
  Santificado seja vós, Console
  Venha a nós o vosso array[10]
  E seja feita, sim, {vossa chave}
  Assim no if{} como no else{}
  O for (nosso; de cada dia; nos dai hoje++)
  Debugai as nossas sentenças
  Assim como nós colocamos o ponto e vígula esquecido;
          E não nos deixeis errar identação
  Mas livrai-nos das funções recursivas
  A main()
*/