package com.uca.parser;

public class Symbol {

    private String name;
    private SymbolTable.Type type;
    private int level;
    private int address;

    public Symbol(String name, SymbolTable.Type type){
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolTable.Type getType() {
        return type;
    }

    public void setType(SymbolTable.Type type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}
