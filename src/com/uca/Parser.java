package com.uca;

import java.util.Stack;

public class Parser {

    private Scanner scanner;
    private Token token;
    private Stack<SymbolTable> symbolTables = new Stack<>();
    private SymbolTable.Type type;

    public Parser(Scanner scanner){
        this.scanner = scanner;
    }

    public void parse(){
        getToken();
        program();
    }

    private void program(){
        symbolTables.push(new SymbolTable(null));
        while (type()){
            declarations();
            if (!matches(Tag.SEMICOLON)){
                System.out.println("Error: Falta ;");
            }
        }
        while (matches(Tag.FUNCTION)){
            type();
            if (is(Tag.IDENTIFIER)){
                symbolTables.peek().add(token.getLexeme(), SymbolTable.Type.FUNCTION);
                getToken();
            }
            addSymbolTable();
            parameters();
            functionBlock();
            removeSymbolTable();
        }
        main();
    }

    private void main(){
        while (!matches(Tag.POINT)){
            statement();
        }
    }

    private void block(){
        addSymbolTable();
        if (!matches(Tag.L_BRACE)){
            System.out.println("Error: Falta {");
        }
        while (!matches(Tag.R_BRACE)){
            statement();
        }
        removeSymbolTable();
    }

    private void functionBlock(){
        addSymbolTable();
        if (!matches(Tag.L_BRACE)){
            System.out.println("Error: Falta {");
        }
        while (!matches(Tag.RETURN)){
            statement();
        }
        expression();
        matches(Tag.SEMICOLON);
        matches(Tag.R_BRACE);
        removeSymbolTable();
    }

    private void statement(){
        if (type()){
            declarations();
            if (!matches(Tag.SEMICOLON)){
                System.out.println("Error: Falta ;");
            }
        }else if (location()){
            assignment();
            if (!matches(Tag.SEMICOLON)){
                System.out.println("Error: Falta ;");
            }
        }else if (matches(Tag.IF)){
            matches(Tag.L_PARENTHESIS);
            conditions();
            matches(Tag.R_PARENTHESIS);
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.IFNOT)){
            matches(Tag.L_PARENTHESIS);
            conditions();
            matches(Tag.R_PARENTHESIS);
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.FOR)){
            matches(Tag.L_PARENTHESIS);
            location();
            assignment();
            matches(Tag.SEMICOLON);
            conditions();
            matches(Tag.SEMICOLON);
            location();
            assignment();
            matches(Tag.R_PARENTHESIS);
            block();
        }else if (matches(Tag.WHILE)){
            matches(Tag.L_PARENTHESIS);
            conditions();
            matches(Tag.R_PARENTHESIS);
            block();
        }else if (matches(Tag.DO)){
            block();
            matches(Tag.WHILE);
            matches(Tag.L_PARENTHESIS);
            conditions();
            matches(Tag.R_PARENTHESIS);
        }else if (matches(Tag.CALL)){
            functionCall();
            matches(Tag.SEMICOLON);
        }
    }

    private void parameters(){
        matches(Tag.L_PARENTHESIS);
        type();
        declaration();
        while (matches(Tag.COLON)){
            type();
            declaration();
        }
        matches(Tag.R_PARENTHESIS);
    }

    private void arguments(){
        matches(Tag.L_PARENTHESIS);
        expression();
        while (matches(Tag.COLON)){
            expression();
        }
        matches(Tag.R_PARENTHESIS);
    }

    private void conditions(){
        condition();
        while (matches(Tag.AND) || matches(Tag.OR)){
            condition();
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
    }

    private void declaration(){
        if (!is(Tag.IDENTIFIER)){
            System.out.println("Error: debe ser identificador "+token.getLexeme());
        }
        symbolTables.peek().add(token.getLexeme(), type);
        getToken();
    }

    private void assignment(){
        if (!matches(Tag.EQUAL)){
            System.out.println("Error: Se esperaba operador de asignacion =");
        }
        expression();

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
        if (matches(Tag.INTEGER) || matches(Tag.DECIMAL) || matches(Tag.STRING) || matches(Tag.CHARACTER) || matches(Tag.TRUE) || matches(Tag.FALSE)){
        }else if(location()){
        }else if(matches(Tag.L_PARENTHESIS)){
            expression();
            if (!matches(Tag.R_PARENTHESIS)){
                System.out.println("Error: Falta parentesis de cierre");
            }
        }else if(matches(Tag.CALL)){
            functionCall();
        }else{
            System.out.println("Error: no es factor");
        }
    }

    private void arrayOperator(){
        matches(Tag.L_BRACKET);
        expression();
        matches(Tag.R_BRACKET);
    }

    private void functionCall(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                System.out.println("Error: no se ha declarado la funcion "+token.getLexeme());
            }else if (symbol.getType() != SymbolTable.Type.FUNCTION){
                System.out.println("Error: identificador debe ser funcion");
            }
            getToken();
        }
        arguments();
    }

    private boolean type(){
        if (basicType()){
            this.type = SymbolTable.Type.VARIABLE;
            return true;
        }else if (matches(Tag.ARRAY)){
            arrayOperator();
            basicType();
            this.type = SymbolTable.Type.ARRAY;
            return true;
        }
        return false;
    }

    private boolean basicType(){
        if (matches(Tag.INT) || matches(Tag.DEC) || matches(Tag.STR) || matches(Tag.CHAR) || matches(Tag.BOO)){
            return true;
        }
        return false;
    }

    private void relational(){
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL)){

        }else{
            System.out.println("Error: no es operador relacional");
        }
    }

    private boolean location(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                System.out.println("Error: no se ha declarado la variable "+token.getLexeme());
            }else if (symbol.getType() == SymbolTable.Type.ARRAY) {
                getToken();
                arrayOperator();
                return true;
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
        if (token == null){
            token = new Token(Tag.POINT);
        }
    }

    private void addSymbolTable(){
        symbolTables.push(new SymbolTable(symbolTables.peek()));
    }

    private void removeSymbolTable(){
        symbolTables.pop();
    }
}