package br.edu.ifsp.pep.csss;

import br.edu.ifsp.pep.csss.TabelaDeSimbolos.TipoCSSSharp;
import br.edu.ifsp.pep.lexico.CssSharpBaseVisitor;
import br.edu.ifsp.pep.lexico.CssSharpParser;

public class CSSSharpSemantico extends CssSharpBaseVisitor<Void> {
    
    private TabelaDeSimbolos tabela;
    
    @Override
    public Void visitPrograma(CssSharpParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }
    
    @Override
    public Void visitDeclaracaoVariavel(CssSharpParser.DeclaracaoVariavelContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        
        // Verifica se a variável já foi declarada
        if (tabela.existe(nomeVar)) {
            CSSSharpSemanticoUtils.adicionarErroSemantico(
                ctx.PALAVRA().getSymbol(),
                "variável '" + nomeVar + "' já foi declarada anteriormente"
            );
        } else {
            // Verifica o tipo do valor sendo atribuído
            TipoCSSSharp tipo = CSSSharpSemanticoUtils.verificarTipoValor(ctx.valor(), tabela);
            
            if (tipo == TipoCSSSharp.INVALIDO) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.valor().start,
                    "valor inválido para a variável '" + nomeVar + "'"
                );
            } else {
                // Captura o valor textual para armazenar na tabela
                String valor = ctx.valor().getText();
                tabela.adicionar(nomeVar, tipo, valor);
            }
        }
        
        return super.visitDeclaracaoVariavel(ctx);
    }
    
    @Override
    public Void visitAtribuicao(CssSharpParser.AtribuicaoContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        
        // Verifica se a variável foi declarada
        if (!tabela.existe(nomeVar)) {
            CSSSharpSemanticoUtils.adicionarErroSemantico(
                ctx.PALAVRA().getSymbol(),
                "variável '" + nomeVar + "' não foi declarada"
            );
        } else {
            // Verifica se o tipo do valor é compatível
            TipoCSSSharp tipoVar = tabela.verificar(nomeVar);
            TipoCSSSharp tipoValor = CSSSharpSemanticoUtils.verificarTipoValor(ctx.valor(), tabela);
            
            if (tipoValor == TipoCSSSharp.INVALIDO) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.valor().start,
                    "valor inválido na atribuição à variável '" + nomeVar + "'"
                );
            } else if (tipoVar != tipoValor) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.valor().start,
                    "tipo incompatível: variável '" + nomeVar + "' é do tipo " + 
                    tipoVar + " mas recebeu valor do tipo " + tipoValor
                );
            }
        }
        
        return super.visitAtribuicao(ctx);
    }
    
    @Override
    public Void visitRegra(CssSharpParser.RegraContext ctx) {
        // Valida o seletor
        if (ctx.seletor() != null) {
            visitSeletor(ctx.seletor());
        }
        
        // Valida as propriedades
        if (ctx.propriedades() != null) {
            visitPropriedades(ctx.propriedades());
        }
        
        return null;
    }
    
    @Override
    public Void visitSeletor(CssSharpParser.SeletorContext ctx) {
        if (ctx.TAG() != null) {
            String tag = ctx.TAG().getText();
            if (!CSSSharpSemanticoUtils.tagValida(tag)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.TAG().getSymbol(),
                    "tag HTML '" + tag + "' não é válida"
                );
            }
        }
        // Seletores com HASHTAG (id) e PONTO (classe) são sempre válidos
        // PALAVRA também é válida (pode ser um seletor customizado)
        
        return super.visitSeletor(ctx);
    }
    
    @Override
    public Void visitPropriedades(CssSharpParser.PropriedadesContext ctx) {
        // Itera sobre cada propriedade CSS
        for (int i = 0; i < ctx.ATRIBUTOS().size(); i++) {
            String propriedade = ctx.ATRIBUTOS(i).getText();
            CssSharpParser.ValoresContext valores = ctx.valores(i);
            
            // Verifica se a propriedade é válida
            if (!CSSSharpSemanticoUtils.propriedadeValida(propriedade)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.ATRIBUTOS(i).getSymbol(),
                    "propriedade CSS '" + propriedade + "' não é válida ou não é suportada"
                );
            }
            
            // Valida os valores da propriedade
            if (valores != null) {
                for (CssSharpParser.ValorContext valor : valores.valor()) {
                    TipoCSSSharp tipo = CSSSharpSemanticoUtils.verificarTipoValor(valor, tabela);
                    if (tipo == TipoCSSSharp.INVALIDO) {
                        CSSSharpSemanticoUtils.adicionarErroSemantico(
                            valor.start,
                            "valor inválido para a propriedade '" + propriedade + "'"
                        );
                    }
                }
            }
        }
        
        return super.visitPropriedades(ctx);
    }
    
    @Override
    public Void visitCondicional(CssSharpParser.CondicionalContext ctx) {
        // Valida o if statement
        if (ctx.ifStatement() != null) {
            visitIfStatement(ctx.ifStatement());
        }
        
        // Valida os elseif statements
        if (ctx.elseifStatement() != null) {
            for (CssSharpParser.ElseifStatementContext elseif : ctx.elseifStatement()) {
                visitElseifStatement(elseif);
            }
        }
        
        // Valida o else statement
        if (ctx.elseStatement() != null) {
            visitElseStatement(ctx.elseStatement());
        }
        
        return null;
    }
    
    @Override
    public Void visitIfStatement(CssSharpParser.IfStatementContext ctx) {
        // Valida a expressão lógica ou variável
        if (ctx.expressaoLogica() != null) {
            visitExpressaoLogica(ctx.expressaoLogica());
        } else if (ctx.PALAVRA() != null) {
            String nomeVar = ctx.PALAVRA().getText();
            if (!tabela.existe(nomeVar)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.PALAVRA().getSymbol(),
                    "variável '" + nomeVar + "' usada na condição não foi declarada"
                );
            }
        }
        
        // Valida a regra CSS dentro do if
        if (ctx.regra() != null) {
            visitRegra(ctx.regra());
        }
        
        return null;
    }
    
    @Override
    public Void visitExpressaoLogica(CssSharpParser.ExpressaoLogicaContext ctx) {
        // Valida os operandos da expressão lógica
        if (ctx.PALAVRA(0) != null) {
            String var1 = ctx.PALAVRA(0).getText();
            if (!tabela.existe(var1) && !CSSSharpSemanticoUtils.palavraChaveValida(var1)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.PALAVRA(0).getSymbol(),
                    "variável '" + var1 + "' não foi declarada"
                );
            }
        }
        
        if (ctx.PALAVRA(1) != null) {
            String var2 = ctx.PALAVRA(1).getText();
            if (!tabela.existe(var2) && !CSSSharpSemanticoUtils.palavraChaveValida(var2)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.PALAVRA(1).getSymbol(),
                    "variável '" + var2 + "' não foi declarada"
                );
            }
        }
        
        return super.visitExpressaoLogica(ctx);
    }
    
    @Override
    public Void visitSwitchCase(CssSharpParser.SwitchCaseContext ctx) {
        // Valida a variável do switch
        if (ctx.PALAVRA() != null) {
            String nomeVar = ctx.PALAVRA().getText();
            if (!tabela.existe(nomeVar)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.PALAVRA().getSymbol(),
                    "variável '" + nomeVar + "' usada no switch não foi declarada"
                );
            }
        }
        
        // Valida cada case statement
        if (ctx.caseStatement() != null) {
            for (CssSharpParser.CaseStatementContext caseStmt : ctx.caseStatement()) {
                visitCaseStatement(caseStmt);
            }
        }
        
        // Valida o default statement
        if (ctx.defaultStatement() != null) {
            visitDefaultStatement(ctx.defaultStatement());
        }
        
        return null;
    }
    
    @Override
    public Void visitCaseStatement(CssSharpParser.CaseStatementContext ctx) {
        // Valida a regra CSS dentro do case
        if (ctx.regra() != null) {
            visitRegra(ctx.regra());
        }
        
        return super.visitCaseStatement(ctx);
    }
    
    @Override
    public Void visitDefaultStatement(CssSharpParser.DefaultStatementContext ctx) {
        // Valida a regra CSS dentro do default
        if (ctx.regra() != null) {
            visitRegra(ctx.regra());
        }
        
        return super.visitDefaultStatement(ctx);
    }
    
    @Override
    public Void visitCor(CssSharpParser.CorContext ctx) {
        if (!CSSSharpSemanticoUtils.verificarCor(ctx)) {
            CSSSharpSemanticoUtils.adicionarErroSemantico(
                ctx.start,
                "formato de cor inválido"
            );
        }
        
        return super.visitCor(ctx);
    }
}
