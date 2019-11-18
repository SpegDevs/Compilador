package com.uca.parser;

public class Symbol {

    private String name;
    private SymbolTable.Type type;
    private SymbolTable.DataType dataType;
    private int level;
    private int address;
    private int params;

    public Symbol(String name, SymbolTable.Type type, SymbolTable.DataType dataType, int level, int address) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.level = level;
        this.address = address;
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

    public SymbolTable.DataType getDataType() {
        return dataType;
    }

    public void setDataType(SymbolTable.DataType dataType) {
        this.dataType = dataType;
    }

    public int getParams() {
        return params;
    }

    public void setParams(int params) {
        this.params = params;
    }
}
