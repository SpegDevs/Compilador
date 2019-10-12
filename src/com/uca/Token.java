package com.uca;

public class Token {

    public Tag tag;

    public Token(Tag tag){
        this.tag = tag;
    }

    public String toString(){
        return tag.toString();
    }
}