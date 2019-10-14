package com.uca;

import java.util.HashMap;

public class SymbolTable {

    public enum Type{
        VARIABLE,FUNCTION,ARRAY
    }

    private HashMap<String,Symbol> table = new HashMap<>();
    private SymbolTable previous;

    public SymbolTable(SymbolTable previous){
        this.previous = previous;
    }

    public void add(String id, Type type){
        table.put(id,new Symbol(id,type));
    }

    public Symbol get(String id){
        for (SymbolTable st=this; st!=null; st=st.getPrevious()){
            Symbol symbol = st.getFromTable(id);
            if (symbol != null){
                return symbol;
            }
        }
        return null;
    }

    private Symbol getFromTable(String id){
        return table.get(id);
    }

    public SymbolTable getPrevious(){
        return previous;
    }
}
