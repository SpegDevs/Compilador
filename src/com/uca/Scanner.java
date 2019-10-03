package com.uca;

public class Scanner {

    private Lexicon.Token token;
    private String lexeme;

    private FileManager file;

    public void scan(String fileName){
        file = new FileManager();
        file.openFile(fileName);
        while (!file.isEndOfFile()){
            getToken();
            if (token != null) {
                System.out.println(token.toString());
            }
        }
        file.closeFile();
        System.out.println("Analisis lexicografico finalizado.");
    }

    public void getToken(){
        char c = ' ';
        lexeme = "";
        token = null;
        while (c == ' ' || c == '\n' || c == '\t'){
            c = getChar();
        }
        if (Character.isLetter(c)){
            lexeme = lexeme.concat(Character.toString(c));
            c = getChar();
            while (Character.isLetter(c)){
                lexeme = lexeme.concat(Character.toString(c));
                c = getChar();
            }
            int i;
            boolean isReservedWord = false;
            for (i=0; i<Parameters.MAX_RESERVED_WORDS; i++){
                if (Lexicon.getReservedWordsLexemes()[i].equals(lexeme)){
                    isReservedWord = true;
                    break;
                }
            }
            if (isReservedWord) {
                token = Lexicon.getReservedWordsTokens()[i];
            }else{
                token = Lexicon.Token.IDENTIFIER;
            }
        }else if (Character.isDigit(c)){

        }else {
            token = Lexicon.getSpecialSymbolsTokens()[c];
        }
    }

    public char getChar(){
        if (file.isEndOfFile()){
           return ' ';
        }
        return file.getNextChar();
    }
}
