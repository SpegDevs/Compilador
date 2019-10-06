package com.uca;

public class Scanner {

    private Lexicon.Token token;
    private String lexeme;
    private char c = ' ';

    private FileManager inputFile;
    private FileManager outputFile;

    public void scan(String fileName){
        inputFile = new FileManager(fileName);
        inputFile.openFile();

        outputFile = new FileManager("output.txt");
        outputFile.createFile();
        outputFile.clearFile();

        System.out.println("Tokens:");
        while (!inputFile.isEndOfFile()){
            getToken();
            if (token != null) {
                System.out.println(lexeme+" -> "+token.toString());
                outputFile.writeLine(lexeme+" -> "+token.toString());
            }
        }

        outputFile.closeFile();
        inputFile.closeFile();
        System.out.println("Analisis lexicografico finalizado.");
    }

    private void getToken(){
        lexeme = "";
        token = null;
        while (Character.isWhitespace(c)){
            c = getChar();
        }
        if (Character.isLetter(c) || c=='_'){
            addToLexeme(c);
            c = getChar();
            while (Character.isLetter(c) || Character.isDigit(c)){
                addToLexeme(c);
                c = getChar();
            }
            if (isReservedWord()) {
                token = Lexicon.getReservedWordToken(lexeme);
            }else{
                token = Lexicon.Token.IDENTIFIER;
            }
        }else if (Character.isDigit(c)){
            boolean decimal=false;
            addToLexeme(c);
            c = getChar();
            while (Character.isDigit(c)){
                addToLexeme(c);
                c = getChar();
            }
            if (c == '.'){
                decimal = true;
                addToLexeme(c);
                c = getChar();
                while (Character.isDigit(c)){
                    addToLexeme(c);
                    c = getChar();
                }
            }
            if (decimal){
                token = Lexicon.Token.DECIMAL;
            }else {
                token = Lexicon.Token.DIGIT;
            }
        }else {
            token = Lexicon.getSpecialSymbolsTokens()[c];
            addToLexeme(c);
            c = getChar();
        }
    }

    private boolean isReservedWord(){
        /*int index = Tools.binarySearch(Lexicon.getReservedWordsLexemes(), lexeme);
        if (index == -1){
            return false;
        }*/
        if (Lexicon.getReservedWordToken(lexeme) == null){
            return false;
        }
        return true;
    }

    private void addToLexeme(char character){
        lexeme = lexeme.concat(Character.toString(character));
    }

    private char getChar(){
        return inputFile.getNextChar();
    }
}
