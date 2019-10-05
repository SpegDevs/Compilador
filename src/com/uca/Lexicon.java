package com.uca;

import java.util.Arrays;
import java.util.HashMap;

import static com.uca.Lexicon.Token.*;

public class Lexicon {

    public enum Token{
        NULL,IF,WHILE,VAR,PLUS,IDENTIFIER,SEMICOLON
    }

    private static HashMap<String,Token> reservedWordsTokens = new HashMap<>();
    private static String[] reservedWordsLexemes = new String[]{"if","while","var"};
    private static Token[] specialSymbolsTokens = new Token[255];

    public static void init(){
        initializeReservedWords();
        initializeSpecialSymbols();
    }

    private static void initializeReservedWords(){
        Arrays.sort(reservedWordsLexemes);
        reservedWordsTokens.put("if",IF);
        reservedWordsTokens.put("while",WHILE);
        reservedWordsTokens.put("var",VAR);
    }

    private static void initializeSpecialSymbols(){
        for (int i=0; i<255; i++){
            specialSymbolsTokens[i] = NULL;
        }
        specialSymbolsTokens[43] = PLUS;
        specialSymbolsTokens[59] = SEMICOLON;
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
