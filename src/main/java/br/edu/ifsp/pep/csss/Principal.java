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

        // analise semantica
        CSSSharpSemantico semantico = new CSSSharpSemantico();
        semantico.visitPrograma(arvore);

        // se diver qualuqer erro semantico ele apresenta o erro e aborta.
        if (!CSSSharpSemanticoUtils.errosSemanticos.isEmpty()) {
            System.err.println("Erros semânticos encontrados:");
            for (String erro : CSSSharpSemanticoUtils.errosSemanticos) {
                System.err.println(erro);
            }
            System.err.println("\nCompilação abortada devido a erros semânticos.");
            return;
        }

        // geradr de codigo
        CSSSharpGeradorCSS gerador = new CSSSharpGeradorCSS();
        gerador.visitPrograma(arvore);

        try (PrintWriter pw = new PrintWriter(args[1])) {
            pw.print(gerador.getCodigoGerado());
            System.out.println("Compilação concluída");
            System.out.println("Arquivo CSS gerado: " + args[1]);
        }
    }
}
