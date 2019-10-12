package com.uca;

import java.util.HashMap;

public class SymbolTable {

    public enum Type{
        VARIABLE,PROCEDURE
    }

    private static HashMap<String,Symbol> table = new HashMap<>();

    public static void init(){

    }

    public static void add(String id, Type type){
        table.put(id,new Symbol(id,type));
    }

    public static boolean exists(String id){
        if (table.get(id) != null){
            return true;
        }
        return false;
    }

    public static Symbol get(String id){
        return table.get(id);
    }
}
