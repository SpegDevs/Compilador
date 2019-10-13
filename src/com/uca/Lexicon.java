package com.uca;

import java.util.Arrays;
import java.util.HashMap;

public class Lexicon {

    private static HashMap<String,Token> reservedWordsTokens = new HashMap<>();
    private static String[] reservedWordsLexemes = new String[]{"if","while","var","ifnot","dowhile","int","str","dec","char","boo","arr","in","out","Length","Pal","Noc","MayorA","RandomI","Factorial","Pow","Ceil","Floor","ret"};
    private static Token[] specialSymbolsTokens = new Token[255];

    public static void init(){
        initializeReservedWords();
        initializeSpecialSymbols();
    }

    private static void initializeReservedWords(){
        Arrays.sort(reservedWordsLexemes);

        reservedWordsTokens.put("int", new Token(Tag.INT));
        reservedWordsTokens.put("dec", new Token(Tag.DEC));
        reservedWordsTokens.put("str", new Token(Tag.STR));
        reservedWordsTokens.put("char", new Token(Tag.CHAR));
        reservedWordsTokens.put("boo", new Token(Tag.BOO));
        reservedWordsTokens.put("arr", new Token(Tag.ARRAY));

        reservedWordsTokens.put("in", new Token(Tag.IN));
        reservedWordsTokens.put("out", new Token(Tag.OUT));
        reservedWordsTokens.put("if", new Token(Tag.IF));
        reservedWordsTokens.put("else", new Token(Tag.ELSE));
        reservedWordsTokens.put("while", new Token(Tag.WHILE));
        reservedWordsTokens.put("ifnot", new Token(Tag.IFNOT));
        reservedWordsTokens.put("do", new Token(Tag.DO));
        reservedWordsTokens.put("for", new Token(Tag.FOR));
        reservedWordsTokens.put("return", new Token(Tag.RETURN));
        reservedWordsTokens.put("true", new Token(Tag.TRUE));
        reservedWordsTokens.put("false", new Token(Tag.FALSE));

        reservedWordsTokens.put("Length", new Token(Tag.LENGTH));
        reservedWordsTokens.put("Pal", new Token(Tag.PALINDROME));
        reservedWordsTokens.put("Noc", new Token(Tag.CONCURRENCIA));
        reservedWordsTokens.put("MayorA", new Token(Tag.MAYORA));
        reservedWordsTokens.put("MenorA", new Token(Tag.MENORA));
        reservedWordsTokens.put("RandomI", new Token(Tag.RANDOM));
        reservedWordsTokens.put("Factorial", new Token(Tag.FACTORIAL));
        reservedWordsTokens.put("Pow", new Token(Tag.POW));
        reservedWordsTokens.put("Ceil", new Token(Tag.CEIL));
        reservedWordsTokens.put("Floor", new Token(Tag.FLOOR));
    }

    private static void initializeSpecialSymbols(){
        for (int i=0; i<255; i++){
            specialSymbolsTokens[i] = null;
        }
        specialSymbolsTokens[34] = new Token(Tag.DOUBLE_QUOTE);
        specialSymbolsTokens[38] = new Token(Tag.AMPERSAND);
        specialSymbolsTokens[39] = new Token(Tag.SINGLE_QUOTE);
        specialSymbolsTokens[40] = new Token(Tag.L_PARENTHESIS);
        specialSymbolsTokens[41] = new Token(Tag.R_PARENTHESIS);
        specialSymbolsTokens[42] = new Token(Tag.MULTIPLICATION);
        specialSymbolsTokens[43] = new Token(Tag.PLUS);
        specialSymbolsTokens[44] = new Token(Tag.COLON);
        specialSymbolsTokens[45] = new Token(Tag.MINUS);
        specialSymbolsTokens[46] = new Token(Tag.POINT);
        specialSymbolsTokens[47] = new Token(Tag.DIVISION);
        specialSymbolsTokens[59] = new Token(Tag.SEMICOLON);
        specialSymbolsTokens[60] = new Token(Tag.LESS_THAN);
        specialSymbolsTokens[61] = new Token(Tag.EQUAL);
        specialSymbolsTokens[62] = new Token(Tag.GREATER_THAN);
        specialSymbolsTokens[91] = new Token(Tag.L_BRACKET);
        specialSymbolsTokens[95] = new Token(Tag.UNDERSCORE);
        specialSymbolsTokens[93] = new Token(Tag.R_BRACKET);
        specialSymbolsTokens[123] = new Token(Tag.L_BRACE);
        specialSymbolsTokens[124] = new Token(Tag.PIPE);
        specialSymbolsTokens[125] = new Token(Tag.R_BRACE);
    }

    public static String[] getReservedWordsLexemes() {
        return reservedWordsLexemes;
    }

    public static Token[] getSpecialSymbolsTokens() {
        return specialSymbolsTokens;
    }

    public static Token getReservedWordToken(String lexeme){
        return reservedWordsTokens.get(lexeme);
    }
}
