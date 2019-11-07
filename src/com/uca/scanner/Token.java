package com.uca.scanner;

public class Token {

    private Tag tag;
    private String lexeme;
    private int line;

    public Token(Tag tag){
        this.tag = tag;
    }

    public Token(Tag tag, String lexeme){
        this.tag = tag;
        this.lexeme = lexeme;
    }

    public String toString(){
        return tag.toString();
    }

    public Tag getTag(){
        return tag;
    }

    public String getLexeme(){
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}