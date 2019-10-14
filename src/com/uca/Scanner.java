package com.uca;

public class Scanner {

    private Token token;
    private String lexeme;
    private char c = ' ';
    private boolean comment;

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

    private void readNextToken(){
        lexeme = "";
        comment = false;
        token = null;
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
                    token = createToken(Tag.UNDERSCORE);
                }else {
                    token = createToken(Tag.IDENTIFIER);
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
                token = createToken(Tag.DECIMAL);
            } else {
                token = createToken(Tag.INTEGER);
            }
        }else if (c == '.'){
            addToLexeme(c);
            c = getChar();
            if (c == '|'){
                c = getChar();
                while (c != '\n'){
                    c = getChar();
                }
                comment = true;
            }else if (c == '-'){
                c = getChar();
                int state = 0;
                boolean finished = false;
                while (!inputFile.isEndOfFile()){
                    c = getChar();
                    if (state==0 && c=='-'){
                        state = 1;
                        continue;
                    }
                    if (state==1 && c=='.'){
                        finished = true;
                        break;
                    }else{
                        state = 0;
                    }
                }
                if (!finished){
                    ErrorLog.logError("Error: Falta cerrar el comentario con -. Linea: "+inputFile.getLineCount());
                }
                comment = true;
                c = getChar();
            }else{
                token = createToken(Tag.POINT);
            }
        }else if (c == '"'){
            addToLexeme(c);
            c = getChar();
            while (c != '"'){
                addToLexeme(c);
                c = getChar();
                if (inputFile.isEndOfFile()){
                    ErrorLog.logError("Error: Falta \" de cierre"+inputFile.getLineCount());
                }
            }
            addToLexeme(c);
            token = createToken(Tag.STRING);
            c = getChar();
        }else if (c == '\''){
            addToLexeme(c);
            c = getChar();
            addToLexeme(c);
            c = getChar();
            if (c != '\''){
                ErrorLog.logError("Error: Caracter debe terminar con ' Linea: "+inputFile.getLineCount());
            }else {
                addToLexeme(c);
                token = createToken(Tag.CHARACTER);
            }
            c = getChar();
        } else {
            if (c == '<'){
                addToLexeme(c);
                c = getChar();
                if (c == '='){
                    addToLexeme(c);
                    token = createToken(Tag.LESS_THAN_EQUAL);
                    c = getChar();
                }else{
                    token = createToken(Tag.LESS_THAN);
                }
            }else if (c == '>'){
                addToLexeme(c);
                c = getChar();
                if (c == '='){
                    addToLexeme(c);
                    token = createToken(Tag.GREATER_THAN_EQUAL);
                    c = getChar();
                }else{
                    token = createToken(Tag.GREATER_THAN);
                }
            }else if (c == '='){
                addToLexeme(c);
                c = getChar();
                if(c == '='){
                    addToLexeme(c);
                    token = createToken(Tag.EQUAL_EQUAL);
                    c = getChar();
                }else{
                    token = createToken(Tag.EQUAL);
                }
            }else if (c == '!'){
                addToLexeme(c);
                c = getChar();
                if (c == '='){
                    addToLexeme(c);
                    token = createToken(Tag.NOT_EQUAL);
                    c = getChar();
                }else{
                    token = createToken(Tag.NOT);
                }
            }else if (c == '&'){
                addToLexeme(c);
                c = getChar();
                if (c == '&'){
                    addToLexeme(c);
                    token = createToken(Tag.AND);
                    c = getChar();
                }else{
                    token = createToken(Tag.AMPERSAND);
                }
            }else if (c == '|'){
                addToLexeme(c);
                c = getChar();
                if (c == '|'){
                    addToLexeme(c);
                    token = createToken(Tag.OR);
                    c = getChar();
                }else{
                    token = createToken(Tag.PIPE);
                }
            }
            else{
                addToLexeme(c);
                if (Lexicon.getSpecialSymbolsTokens()[c] != null) {
                    token = createToken(Lexicon.getSpecialSymbolsTokens()[c].getTag());
                }
                if (token == null) {
                    ErrorLog.logError("Error: No se reconoce el simbolo \"" + c + "\" Linea: " + inputFile.getLineCount());
                }
                c = getChar();
            }
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

    private void addToken(String lexeme, Token token){
        if (token != null) {
            System.out.println(lexeme+" -> "+token.toString());
            if (outputFile != null) {
                outputFile.writeLine(lexeme + " -> " + token.toString());
            }
        }
    }

    private Token createToken(Tag tag){
        return new Token(tag,lexeme);
    }

    public Token getToken(){
        if (inputFile.isEndOfFile()){
            return createToken(Tag.POINT);
        }
        readNextToken();
        while (comment){
            readNextToken();
        }
        addToken(lexeme, token);
        return token;
    }

    private char getChar(){
        return inputFile.getNextChar();
    }

    public int getLine(){
        return inputFile.getLineCount();
    }

    public boolean isEndOfFile(){
        return inputFile.isEndOfFile();
    }
}
