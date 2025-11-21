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
        
        // verifica se a variavel ja foi declarada
        if (tabela.existe(nomeVar)) {
            CSSSharpSemanticoUtils.adicionarErroSemantico(
                ctx.PALAVRA().getSymbol(),
                "variável '" + nomeVar + "' já foi declarada anteriormente"
            );
        } else {
            // verifica tipo valor = atribuído
            TipoCSSSharp tipo = CSSSharpSemanticoUtils.verificarTipoValor(ctx.valor(), tabela);
            
            if (tipo == TipoCSSSharp.INVALIDO) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.valor().start,
                    "valor inválido para a variável '" + nomeVar + "'"
                );
            } else {
                String valor = ctx.valor().getText();
                tabela.adicionar(nomeVar, tipo, valor);
            }
        }
        
        return super.visitDeclaracaoVariavel(ctx);
    }
    
    @Override
    public Void visitAtribuicao(CssSharpParser.AtribuicaoContext ctx) {
        String nomeVar = ctx.PALAVRA().getText();
        
        // verifica se foi declarada
        if (!tabela.existe(nomeVar)) {
            CSSSharpSemanticoUtils.adicionarErroSemantico(
                ctx.PALAVRA().getSymbol(),
                "variável '" + nomeVar + "' não foi declarada"
            );
        } else {
            // verifica se o tipo é compatível
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
        // valida seletor e propriedade
        if (ctx.seletor() != null) {
            visitSeletor(ctx.seletor());
        }

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
        for (int i = 0; i < ctx.ATRIBUTOS().size(); i++) {
            String propriedade = ctx.ATRIBUTOS(i).getText();
            CssSharpParser.ValoresContext valores = ctx.valores(i);
            
            // verifica se é válido
            if (!CSSSharpSemanticoUtils.propriedadeValida(propriedade)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.ATRIBUTOS(i).getSymbol(),
                    "propriedade CSS '" + propriedade + "' não é válida ou não é suportada"
                );
            }
            
            // valida os valores
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
        // valida if
        if (ctx.ifStatement() != null) {
            visitIfStatement(ctx.ifStatement());
        }
        
        // valide elseif
        if (ctx.elseifStatement() != null) {
            for (CssSharpParser.ElseifStatementContext elseif : ctx.elseifStatement()) {
                visitElseifStatement(elseif);
            }
        }
        
        // valida else
        if (ctx.elseStatement() != null) {
            visitElseStatement(ctx.elseStatement());
        }
        
        return null;
    }
    
    @Override
    public Void visitIfStatement(CssSharpParser.IfStatementContext ctx) {
        // valida expressão lógica ou variável
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
        
        // valida regra
        if (ctx.regra() != null) {
            visitRegra(ctx.regra());
        }
        
        return null;
    }
    
    @Override
    public Void visitExpressaoLogica(CssSharpParser.ExpressaoLogicaContext ctx) {
        // Valida o operando1 da expressão lógica
        if (ctx.PALAVRA(0) != null) {
            String var1 = ctx.PALAVRA(0).getText();
            if (!tabela.existe(var1) && !CSSSharpSemanticoUtils.palavraChaveValida(var1)) {
                CSSSharpSemanticoUtils.adicionarErroSemantico(
                    ctx.PALAVRA(0).getSymbol(),
                    "variável '" + var1 + "' não foi declarada"
                );
            }
        }

        // Valida o operando2 da expressão lógica
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
        
        // Valida cada case
        if (ctx.caseStatement() != null) {
            for (CssSharpParser.CaseStatementContext caseStmt : ctx.caseStatement()) {
                visitCaseStatement(caseStmt);
            }
        }
        
        // Valida o default
        if (ctx.defaultStatement() != null) {
            visitDefaultStatement(ctx.defaultStatement());
        }
        
        return null;
    }
    
    @Override
    public Void visitCaseStatement(CssSharpParser.CaseStatementContext ctx) {
        // Verifica se o código do case está correto
        if (ctx.regra() != null) {
            visitRegra(ctx.regra());
        }
        
        return super.visitCaseStatement(ctx);
    }
    
    @Override
    public Void visitDefaultStatement(CssSharpParser.DefaultStatementContext ctx) {
        // Verifica se o código do default está correto
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
