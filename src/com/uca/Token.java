package com.uca;

public class Token {

    private Tag tag;
    private String lexeme;
    private int line;

    public Token(Tag tag){
        this.tag = tag;
    }

    public Token(Tag tag, String lexeme, int line){
        this.tag = tag;
        this.lexeme = lexeme;
        this.line = line;
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

    public int getLine(){
        return line;
    }
}