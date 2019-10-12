package com.uca;

public class Token {

    private Tag tag;
    private String lexeme;

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
}