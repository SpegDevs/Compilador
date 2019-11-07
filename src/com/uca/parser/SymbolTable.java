package com.uca.parser;

import java.util.HashMap;

public class SymbolTable {

    public enum Type {
        VARIABLE, FUNCTION, ARRAY
    }

    public enum DataType {
        INTEGER, DECIMAL, BOOLEAN, STRING, CHARACTER
    }

    private HashMap<String, Symbol> table = new HashMap<>();
    private SymbolTable previous;

    public SymbolTable(SymbolTable previous) {
        this.previous = previous;
    }

    public void add(String id, Type type, DataType dataType, int level, int address) {
        table.put(id, new Symbol(id, type, dataType, level, address));
    }

    public Symbol get(String id) {
        for (SymbolTable st = this; st != null; st = st.getPrevious()) {
            Symbol symbol = st.getFromCurrentTable(id);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    public Symbol getFromCurrentTable(String id) {
        return table.get(id);
    }

    public SymbolTable getPrevious() {
        return previous;
    }

    public void printSymbols(){
        for (String key:table.keySet()){
            Symbol s = table.get(key);
            System.out.println(s.getName()+" "+s.getType().toString()+" "+s.getDataType().toString()+" "+s.getLevel()+" "+s.getAddress());
        }
    }
}
