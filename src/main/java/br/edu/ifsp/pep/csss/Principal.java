package br.edu.ifsp.pep.csss;

import java.io.IOException;

import br.edu.ifsp.pep.lexico.CssSharpLexer;
import br.edu.ifsp.pep.lexico.CssSharpParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

public class Principal {
    public static void main(String args[]) throws IOException {
        CharStream cs = CharStreams.fromFileName(args[0]);
        CssSharpLexer lexer = new CssSharpLexer(cs);

        // Depuração do léxico
        Token t = null;
        while ((t = lexer.nextToken()).getType() != Token.EOF) {
            System.out.println("<" + CssSharpLexer.VOCABULARY.getDisplayName(t.getType()) + "," + t.getText() + ">");
        }

        lexer.reset();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CssSharpParser parser = new CssSharpParser(tokens);
        parser.programa();
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