package br.edu.ifsp.pep.csss;

import java.io.IOException;
import java.io.PrintWriter;

import br.edu.ifsp.pep.lexico.CssSharpLexer;
import br.edu.ifsp.pep.lexico.CssSharpParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Principal {
    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.err.println("Uso: java Principal <arquivo-entrada.csss> <arquivo-saida.css>");
            return;
        }
        
        CharStream cs = CharStreams.fromFileName(args[0]);
        CssSharpLexer lexer = new CssSharpLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CssSharpParser parser = new CssSharpParser(tokens);
        CssSharpParser.ProgramaContext arvore = parser.programa();
        
        // Análise Semântica (1ª passada)
        CSSSharpSemantico semantico = new CSSSharpSemantico();
        semantico.visitPrograma(arvore);
        
        // Imprime erros semânticos, se houver
        if (!CSSSharpSemanticoUtils.errosSemanticos.isEmpty()) {
            System.err.println("Erros semânticos encontrados:");
            for (String erro : CSSSharpSemanticoUtils.errosSemanticos) {
                System.err.println(erro);
            }
            System.err.println("\nCompilação abortada devido a erros semânticos.");
            return;
        }
        
        // Geração de Código CSS (2ª passada)
        CSSSharpGeradorCSS gerador = new CSSSharpGeradorCSS();
        gerador.visitPrograma(arvore);
        
        // Escreve o código CSS gerado no arquivo de saída
        try (PrintWriter pw = new PrintWriter(args[1])) {
            pw.print(gerador.getCodigoGerado());
            System.out.println("Compilação concluída com sucesso!");
            System.out.println("Arquivo CSS gerado: " + args[1]);
        }
    }
}

/*
* codigo do meu professor
*public class Principal {
    public static void main(String args[]) throws IOException {
        CharStream cs = CharStreams.fromFileName(args[0]);
        AlgumaLexer lexer = new AlgumaLexer(cs);

        // Depuração do léxico
        Token t = null;
        while( (t = lexer.nextToken()).getType() != Token.EOF) {
            System.out.println("<" + AlgumaLexer.VOCABULARY.getDisplayName(t.getType()) + "," + t.getText() + ">");
        }

        lexer.reset();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        AlgumaParser parser = new AlgumaParser(tokens);
        parser.programa();
    }
}
* */