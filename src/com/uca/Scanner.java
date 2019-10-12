package com.uca;

public class Scanner {

    private Token token;
    private String lexeme;
    private char c = ' ';

    private FileManager inputFile;
    private FileManager outputFile;

    public void scan(String fileName){
        inputFile = new FileManager(fileName);
        inputFile.openFile();

        outputFile = new FileManager("output/output.txt");
        outputFile.createFile();
        outputFile.clearFile();

        /*System.out.println("Iniciando Analisis lexicografico.");
        System.out.println("Tokens:");
        while (!inputFile.isEndOfFile()){
            getToken();
        }

        outputFile.closeFile();
        inputFile.closeFile();
        System.out.println("Analisis lexicografico finalizado.");*/
    }

    public void close(){
        outputFile.closeFile();
        inputFile.closeFile();
    }

    public Token getToken(){
        lexeme = "";
        token = new Token(Tag.NULL);
        while (Character.isWhitespace(c)){
            c = getChar();
        }
        if (Character.isLetter(c) || c=='_'){
            addToLexeme(c);
            c = getChar();
            while (c == '_' || Character.isLetter(c) || Character.isDigit(c)){
                addToLexeme(c);
                c = getChar();
            }
            if (isReservedWord()) {
                token = Lexicon.getReservedWordToken(lexeme);
            }else{
                if (lexeme.length() > Parameters.MAX_IDENTIFIER_LENGTH){
                    ErrorLog.logError("Error: Identificador \""+lexeme+"\" sobrepasa el maximo de caracteres validos ("+Parameters.MAX_IDENTIFIER_LENGTH+"). Linea: "+inputFile.getLineCount());
                    lexeme = lexeme.substring(0,Parameters.MAX_IDENTIFIER_LENGTH);
                }
                if (lexeme.equals("_")){
                    token = new Token(Tag.UNDERSCORE);
                }else {
                    token = new Token(Tag.IDENTIFIER,lexeme);
                }
            }
        }else if (Character.isDigit(c)){
            boolean decimal=false;
            addToLexeme(c);
            c = getChar();
            while (Character.isDigit(c)){
                addToLexeme(c);
                c = getChar();
            }
            if (lexeme.length() > Parameters.MAX_DIGITS){
                ErrorLog.logError("Error: Numero \""+lexeme+"\" sobrepasa el maximo de digitos validos ("+Parameters.MAX_DIGITS+"). Linea: "+inputFile.getLineCount());
                lexeme = lexeme.substring(0,Parameters.MAX_DIGITS);
            }
            if (c == '.'){
                decimal = true;
                addToLexeme(c);
                c = getChar();
                if (Character.isDigit(c)){
                    while (Character.isDigit(c)){
                        addToLexeme(c);
                        c = getChar();
                    }
                }
                else{
                    ErrorLog.logError("Error: Decimal \""+lexeme+"\" mal escrito. Linea: "+inputFile.getLineCount());
                }
            }
            if (decimal) {
                token = new Token(Tag.DECIMAL);
            } else {
                token = new Token(Tag.INTEGER);
            }
        }else if (c == '.'){
            addToLexeme(c);
            c = getChar();
            if (c == '|'){
                c = getChar();
                while (c != '\n'){
                    c = getChar();
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
                    ErrorLog.logError("Error: Comentario multilinea mal escrito. Linea: "+inputFile.getLineCount());
                }
                c = getChar();
            }else{
                token = new Token(Tag.POINT);
            }
        }else if (c == '"'){
            //String
        }else if (c == '\''){
            //Caracter
        } else {
            if (c == '<'){
                addToLexeme(c);
                c = getChar();
                if (c == '='){
                    token = new Token(Tag.LESS_THAN_EQUAL);
                    addToLexeme(c);
                    c = getChar();
                }else{
                    token = new Token(Tag.LESS_THAN);
                }
            }else if (c == '>'){
                addToLexeme(c);
                c = getChar();
                if (c == '='){
                    addToLexeme(c);
                    token = new Token(Tag.GREATER_THAN_EQUAL);
                    c = getChar();
                }else{
                    token = new Token(Tag.GREATER_THAN);
                }
            }else if(c=='='){
                addToLexeme(c);
                c=getChar();
                if(c=='='){
                    addToLexeme(c);
                    token = new Token(Tag.EQUAL_EQUAL);
                    c=getChar();
                }else{
                    token = new Token(Tag.EQUAL);
                }
            }
            else{
                token = Lexicon.getSpecialSymbolsTokens()[c];
                if (token == null) {
                    ErrorLog.logError("Error: No se reconoce el simbolo \"" + c + "\" Linea: " + inputFile.getLineCount());
                }
                addToLexeme(c);
                c = getChar();
            }
        }
        addToken(lexeme, token);
        return token;
    }

    private boolean isReservedWord(){
        int index = Tools.binarySearch(Lexicon.getReservedWordsLexemes(), lexeme);
        if (index == -1){
            return false;
        }
        return true;
    }

    private void addToLexeme(char character){
        lexeme = lexeme.concat(Character.toString(character));
    }

    private void addToken(String lexeme, Token token){
        if (token != null && token.getTag() != Tag.NULL) {
            System.out.println(lexeme+" -> "+token.toString());
            if (outputFile != null) {
                outputFile.writeLine(lexeme + " -> " + token.toString());
            }
        }
    }

    private char getChar(){
        return inputFile.getNextChar();
    }
}
