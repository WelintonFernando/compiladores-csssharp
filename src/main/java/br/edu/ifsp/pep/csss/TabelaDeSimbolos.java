package br.edu.ifsp.pep.csss;

import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {
    
    public enum TipoCSSSharp {
        NUMERO,         // Valores numéricos (px, vh, vw, pt, rem, em)
        COR,            // Cores (hex, rgb, rgba)
        STRING,         // Strings
        MEDIDA,         // Medidas com unidade
        PALAVRA_CHAVE,  // Palavras-chave CSS (solid, bold, etc)
        INVALIDO
    }
    
    private class EntradaTabelaSimbolos {
        private String nome;
        private TipoCSSSharp tipo;
        private String valor;  // Armazena o valor da variável
        
        private EntradaTabelaSimbolos(String nome, TipoCSSSharp tipo, String valor) {
            this.nome = nome;
            this.tipo = tipo;
            this.valor = valor;
        }
    }
    
    private Map<String, EntradaTabelaSimbolos> tabela;
    
    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }
    
    public void adicionar(String nome, TipoCSSSharp tipo, String valor) {
        tabela.put(nome, new EntradaTabelaSimbolos(nome, tipo, valor));
    }
    
    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }
    
    public TipoCSSSharp verificar(String nome) {
        if (existe(nome)) {
            return tabela.get(nome).tipo;
        }
        return TipoCSSSharp.INVALIDO;
    }
    
    public String obterValor(String nome) {
        if (existe(nome)) {
            return tabela.get(nome).valor;
        }
        return null;
    }
}
