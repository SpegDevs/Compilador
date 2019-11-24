package com.uca.scanner;

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

        reservedWordsTokens.put("if", new Token(Tag.IF));
        reservedWordsTokens.put("ifnot", new Token(Tag.IFNOT));
        reservedWordsTokens.put("else", new Token(Tag.ELSE));
        reservedWordsTokens.put("while", new Token(Tag.WHILE));
        reservedWordsTokens.put("do", new Token(Tag.DO));
        reservedWordsTokens.put("for", new Token(Tag.FOR));
        reservedWordsTokens.put("ret", new Token(Tag.RETURN));
        reservedWordsTokens.put("function", new Token(Tag.FUNCTION));
        reservedWordsTokens.put("call", new Token(Tag.CALL));

        reservedWordsTokens.put("true", new TokenValue<Boolean>(Tag.TRUE, true));
        reservedWordsTokens.put("false", new TokenValue<Boolean>(Tag.FALSE, false));

        reservedWordsTokens.put("Max", new Token(Tag.MAX));
        reservedWordsTokens.put("Min", new Token(Tag.MIN));
        reservedWordsTokens.put("Random", new Token(Tag.RANDOM));
        reservedWordsTokens.put("Factorial", new Token(Tag.FACTORIAL));
        reservedWordsTokens.put("Pow", new Token(Tag.POW));
        reservedWordsTokens.put("Sqrt", new Token(Tag.SQRT));
        reservedWordsTokens.put("Ceil", new Token(Tag.CEIL));
        reservedWordsTokens.put("Floor", new Token(Tag.FLOOR));
        reservedWordsTokens.put("Round", new Token(Tag.ROUND));
        reservedWordsTokens.put("Substring", new Token(Tag.SUBSTRING));

        reservedWordsTokens.put("out", new Token(Tag.OUT));
        reservedWordsTokens.put("inInt", new Token(Tag.IN_INT));
        reservedWordsTokens.put("inDec", new Token(Tag.IN_DEC));
        reservedWordsTokens.put("inCha", new Token(Tag.IN_CHA));
        reservedWordsTokens.put("inStr", new Token(Tag.IN_STR));
        reservedWordsTokens.put("inBoo", new Token(Tag.IN_BOO));

        reservedWordsTokens.put("fileWrite", new Token(Tag.FILE_WRITE));
        reservedWordsTokens.put("fileReadInt", new Token(Tag.FILE_READ_INT));
        reservedWordsTokens.put("fileReadDec", new Token(Tag.FILE_READ_DEC));
        reservedWordsTokens.put("fileReadCha", new Token(Tag.FILE_READ_CHA));
        reservedWordsTokens.put("fileReadStr", new Token(Tag.FILE_READ_STR));
        reservedWordsTokens.put("fileReadBoo", new Token(Tag.FILE_READ_BOO));
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
