package com.uca;

public class Symbol {

    private String name;
    private SymbolTable.Type type;

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
}
