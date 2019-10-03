package com.uca;

import static com.uca.Lexicon.Token.*;

public class Lexicon {

    public enum Token{
        NULL,IF,WHILE,VAR,PLUS,IDENTIFIER,SEMICOLON
    }

    private static String[] reservedWordsLexemes = new String[]{"if","while","var"};
    private static Token[] reservedWordsTokens = new Token[]{IF,WHILE,VAR};
    private static Token[] specialSymbolsTokens = new Token[255];

    public static void initializeSpecialSymbols(){
        for (int i=0; i<255; i++){
            specialSymbolsTokens[i] = NULL;
        }
        specialSymbolsTokens[43] = PLUS;
        specialSymbolsTokens[59] = SEMICOLON;
    }

    public static String[] getReservedWordsLexemes() {
        return reservedWordsLexemes;
    }

    public static Token[] getReservedWordsTokens() {
        return reservedWordsTokens;
    }

    public static Token[] getSpecialSymbolsTokens() {
        return specialSymbolsTokens;
    }
}
