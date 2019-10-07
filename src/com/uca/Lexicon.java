package com.uca;

import java.util.Arrays;
import java.util.HashMap;

import static com.uca.Lexicon.Token.*;

public class Lexicon {

    public enum Token{
        NULL,IF,WHILE,VAR,PLUS,IDENTIFIER,SEMICOLON,INTEGER,STRING,DECIMAL,CHARACTER,BOOLEAN,ARRAY,IN,OUT,LENGTH, PALINDROME,
        CONCURRENCIA,MAYORA,RANDOM,FACTORIAL,POW,CEIL,FLOOR,RETURN,MUL,DIV,PARINI,PARFIN,LLAINI,LLAFIN,CORINI,CORFIN,POINT,COMA,SQUOTE,DQUOTE,
        GUION,GUIONB,IFNOT,DOWHILE,EQUAL,MENOR,MAYOR,DIGIT,BARRA
    }

    private static HashMap<String,Token> reservedWordsTokens = new HashMap<>();
    private static String[] reservedWordsLexemes = new String[]{"if","while","var","ifnot","dowhile","int","str","dec","char","boo","arr","in","out","Length","Pal","Noc","MayorA","RandomI","Factorial","Pow","Ceil","Floor","ret"};
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
        reservedWordsTokens.put("ifnot",IFNOT);
        reservedWordsTokens.put("dowhile",DOWHILE);
        reservedWordsTokens.put("int",INTEGER);
        reservedWordsTokens.put("str",STRING);
        reservedWordsTokens.put("dec",DECIMAL);
        reservedWordsTokens.put("char",CHARACTER);
        reservedWordsTokens.put("boo",BOOLEAN);
        reservedWordsTokens.put("arr",ARRAY);
        reservedWordsTokens.put("in",IN);
        reservedWordsTokens.put("out",OUT);
        reservedWordsTokens.put("Length",LENGTH);
        reservedWordsTokens.put("Pal",PALINDROME);
        reservedWordsTokens.put("Noc",CONCURRENCIA);
        reservedWordsTokens.put("MayorA",MAYORA);
        reservedWordsTokens.put("RandomI",RANDOM);
        reservedWordsTokens.put("Factorial",FACTORIAL);
        reservedWordsTokens.put("Pow",POW);
        reservedWordsTokens.put("Ceil",CEIL);
        reservedWordsTokens.put("Floor",FLOOR);
        reservedWordsTokens.put("ret",RETURN);
    }

    private static void initializeSpecialSymbols(){
        for (int i=0; i<255; i++){
            specialSymbolsTokens[i] = NULL;
        }
        specialSymbolsTokens[34] = DQUOTE;
        specialSymbolsTokens[39] = SQUOTE;
        specialSymbolsTokens[40] = PARINI;
        specialSymbolsTokens[41] = PARFIN;
        specialSymbolsTokens[42] = MUL;
        specialSymbolsTokens[43] = PLUS;
        specialSymbolsTokens[44] = COMA;
        specialSymbolsTokens[45] = GUION;
        specialSymbolsTokens[46] = POINT;
        specialSymbolsTokens[47] = DIV;
        specialSymbolsTokens[59] = SEMICOLON;
        specialSymbolsTokens[60] = MENOR;
        specialSymbolsTokens[61] = EQUAL;
        specialSymbolsTokens[62] = MAYOR;
        specialSymbolsTokens[91] = CORINI;
        specialSymbolsTokens[95] = GUIONB;
        specialSymbolsTokens[93] = CORFIN;
        specialSymbolsTokens[123] = LLAINI;
        specialSymbolsTokens[124] = BARRA;
        specialSymbolsTokens[125] = LLAFIN;
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
