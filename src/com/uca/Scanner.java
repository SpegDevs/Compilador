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
            addToken(lexeme, token);
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
                addToLexeme(c);
                c = getChar();
                if (Character.isDigit(c)){
                    decimal = true;
                    while (Character.isDigit(c)){
                        addToLexeme(c);
                        c = getChar();
                    }
                }
                else{
                    System.out.println("Error: decimal mal escrito");
                }
            }
            if (decimal){
                token = Lexicon.Token.DECIMAL;
            }else {
                token = Lexicon.Token.DIGIT;

            }
        }else if (c == '.'){
            addToLexeme(c);
            c = getChar();
            if (c == '|'){
                c = getChar();
                if (c == '.'){
                    c = getChar();
                    while (c != '\n'){
                        c = getChar();
                    }
                }else{
                    System.out.println("Error: comentario mal escrito");
                }
            }else if (c == '-'){
                c = getChar();
                int state = 0;
                boolean comment = false;
                while (!inputFile.isEndOfFile()){
                    c = getChar();
                    if (state==0 && c=='-'){
                        state = 1;
                        continue;
                    }
                    if (state==1 && c=='.'){
                        comment = true;
                        break;
                    }else{
                        state = 0;
                    }
                }
                if (!comment){
                    System.out.println("Error: comentario multilinea mal escrito");
                }
                c = getChar();
            }else{
                addToken(lexeme, Lexicon.Token.POINT);
            }
        }
        else {
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

    private void addToken(String lexeme, Lexicon.Token token){
        if (token != null) {
            System.out.println(lexeme+" -> "+token.toString());
            outputFile.writeLine(lexeme+" -> "+token.toString());
        }
    }

    private char getChar(){
        return inputFile.getNextChar();
    }
}
