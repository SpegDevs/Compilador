package com.uca;

import java.util.Stack;

public class Parser {

    private Scanner scanner;
    private Token token;
    private Stack<SymbolTable> symbolTables = new Stack<>();

    public Parser(Scanner scanner){
        this.scanner = scanner;
    }

    public void parse(){
        getToken();
        program();
    }

    private void program(){
        main();
    }

    private void main(){
        symbolTables.push(new SymbolTable(null));
        while (!is(Tag.NULL)){
            statement();
        }
    }

    private void block(){
        symbolTables.push(new SymbolTable(symbolTables.peek()));
        if (!matches(Tag.L_BRACE)){
            System.out.println("Error: Falta {");
        }
        while (!matches(Tag.R_BRACE)){
            statement();
            if (is(Tag.NULL)) {
                System.out.println("Error: Falta }");
            }
        }
        symbolTables.pop();
    }

    private void statement(){
        if (type()){
            declarations();
        }
        else if (location()){
            assignment();
        }else if (matches(Tag.IF)){
            matches(Tag.L_PARENTHESIS);
            condition();
            matches(Tag.R_PARENTHESIS);
            block();
        }else if (matches(Tag.IFNOT)){
            matches(Tag.L_PARENTHESIS);
            condition();
            matches(Tag.R_PARENTHESIS);
            block();
        }
    }

    private void condition(){
        expression();
        relational();
        expression();
    }

    private void declarations(){
        declaration();
        while (matches(Tag.COLON)){
            declaration();
        }
        if (!matches(Tag.SEMICOLON)){
            System.out.println("Error: Falta ;");
        }
    }

    private void declaration(){
        if (!is(Tag.IDENTIFIER)){
            System.out.println("Error: debe ser identificador ;");
        }
        symbolTables.peek().add(token.getLexeme(), SymbolTable.Type.VARIABLE);
        getToken();
    }

    private void assignment(){
        if (!matches(Tag.EQUAL)){
            System.out.println("Error: Se esperaba operador de asignacion =");
        }
        expression();
        if (!matches(Tag.SEMICOLON)){
            System.out.println("Error: Falta ;");
        }
    }

    private void expression(){
        term();
        while (matches(Tag.PLUS) || matches(Tag.MINUS)){
            term();
        }
    }

    private void term(){
        factor();
        while (matches(Tag.MULTIPLICATION) || matches(Tag.DIVISION)){
            factor();
        }
    }

    private void factor(){
        if (matches(Tag.INTEGER)){
        }else if(location()){
        }else if(matches(Tag.L_PARENTHESIS)){
            expression();
            if (!matches(Tag.R_PARENTHESIS)){
                System.out.println("Error: Falta parentesis de cierre");
            }
        }else{
            System.out.println("Error: no es factor");
        }
    }

    private boolean type(){
        if (matches(Tag.INT) || matches(Tag.DEC) || matches(Tag.STR) || matches(Tag.CHAR) || matches(Tag.BOO)){
            return true;
        }
        return false;
    }

    private void relational(){
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL)){

        }else{
            System.out.println("Error: no es operador relacional");
        }
    }

    private boolean location(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                System.out.println("Error: no se ha declarado la variable "+token.getLexeme());
            }else if (symbol.getType() != SymbolTable.Type.VARIABLE){
                System.out.println("Error: identificador debe ser variable");
            }
            getToken();
            return true;
        }
        return false;
    }

    private boolean is(Tag tag){
        if (token.getTag() == tag){
            return true;
        }
        return false;
    }

    private boolean matches(Tag tag){
        if (is(tag)){
            getToken();
            return true;
        }
        return false;
    }

    private void getToken(){
        token = scanner.getToken();
    }
}