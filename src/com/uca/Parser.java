package com.uca;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner){
        this.scanner = scanner;
    }

    public void parse(){
        program();
    }

    private void program(){
        statements();
    }

    private void statements(){
        getToken();
        while (!is(Tag.POINT)){
            statement();
            getToken();
        }
    }

    private void statement(){
        if (type()){
            declarations();
        }
        else if (location()){
            assignment();
        }
    }

    private void declarations(){
        declaration();
        getToken();
        while (is(Tag.COLON)){
            declaration();
            getToken();
        }
        if (!is(Tag.SEMICOLON)){
            System.out.println("Error: Falta ;");
        }
    }

    private void declaration(){
        getToken();
        if (!is(Tag.IDENTIFIER)){
            System.out.println("Error: debe ser identificador ;");
        }
        SymbolTable.add(token.getLexeme(), SymbolTable.Type.VARIABLE);
    }

    private void assignment(){
        getToken();
        if (!is(Tag.EQUAL)){
            System.out.println("Error: Se esperaba operador de asignacion =");
        }
        expression();
        if (!is(Tag.SEMICOLON)){
            System.out.println("Error: Falta ;");
        }
    }

    private void expression(){
        getToken();
        term();
        while (is(Tag.PLUS) || is(Tag.MINUS)){
            getToken();
            term();
        }
    }

    private void term(){
        factor();
        getToken();
        while (is(Tag.MULTIPLICATION) || is(Tag.DIVISION)){
            getToken();
            factor();
            getToken();
        }
    }

    private void factor(){
        if (is(Tag.INTEGER)){
        }else if(location()){
        }else if(is(Tag.L_PARENTHESIS)){
            expression();
            if (!is(Tag.R_PARENTHESIS)){
                System.out.println("Error: Falta parentesis de cierre");
            }
        }else{
            System.out.println("Error: no es factor");
        }
    }

    private boolean type(){
        if (is(Tag.INT) || is(Tag.DEC) || is(Tag.STR) || is(Tag.CHAR) || is(Tag.BOO)){
            return true;
        }
        return false;
    }

    private boolean location(){
        if (is(Tag.IDENTIFIER)){
            if (!SymbolTable.exists(token.getLexeme())){
                System.out.println("Error: no se ha declarado la variable "+token.getLexeme());
            }else if (SymbolTable.get(token.getLexeme()).getType() != SymbolTable.Type.VARIABLE){
                System.out.println("Error: identificador debe ser variable");
            }
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

    private void getToken(){
        token = scanner.getToken();
    }
}