package com.uca;

import java.util.Stack;

public class Parser {

    private Scanner scanner;
    private Token token;
    private Stack<SymbolTable> symbolTables = new Stack<>();
    private SymbolTable.Type type;
    private PCodeGenerator pCodeGenerator = new PCodeGenerator();

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
            matchesError(Tag.SEMICOLON,";");
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
        while (!scanner.isEndOfFile()){
            statement();
        }
    }

    private void block(){
        addSymbolTable();
        matchesError(Tag.L_BRACE,"{");
        while (!matches(Tag.R_BRACE)){
            statement();
        }
        removeSymbolTable();
    }

    private void functionBlock(){
        addSymbolTable();
        matchesError(Tag.L_BRACE,"{");
        while (!matches(Tag.RETURN)){
            statement();
        }
        expression();
        matchesError(Tag.SEMICOLON,";");
        matchesError(Tag.R_BRACE,"}");
        removeSymbolTable();
    }

    private void statement(){
        if (type()){
            declarations();
            matchesError(Tag.SEMICOLON,";");
        }else if (location()){
            assignment();
            matchesError(Tag.SEMICOLON,";");
        }else if (matches(Tag.IF)){
            matchesError(Tag.L_PARENTHESIS,"(");
            conditions();
            matchesError(Tag.R_PARENTHESIS,")");
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.IFNOT)){
            matchesError(Tag.L_PARENTHESIS,"(");
            conditions();
            matchesError(Tag.R_PARENTHESIS,")");
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.FOR)){
            matchesError(Tag.L_PARENTHESIS,"(");
            location();
            assignment();
            matchesError(Tag.SEMICOLON,";");
            conditions();
            matchesError(Tag.SEMICOLON,";");
            location();
            assignment();
            matchesError(Tag.R_PARENTHESIS,")");
            block();
        }else if (matches(Tag.WHILE)){
            matchesError(Tag.L_PARENTHESIS,"(");
            conditions();
            matchesError(Tag.R_PARENTHESIS,")");
            block();
        }else if (matches(Tag.DO)){
            block();
            matchesError(Tag.WHILE,"while");
            matchesError(Tag.L_PARENTHESIS,"(");
            conditions();
            matchesError(Tag.R_PARENTHESIS,")");
        }else if (matches(Tag.CALL)){
            functionCall();
            matchesError(Tag.SEMICOLON,";");
        }else{
            printError("Error: No es instruccion "+token.getLexeme()+".");
        }
    }

    private void parameters(){
        matchesError(Tag.L_PARENTHESIS,"(");
        type();
        declaration();
        while (matches(Tag.COLON)){
            type();
            declaration();
        }
        matchesError(Tag.R_PARENTHESIS,")");
    }

    private void arguments(){
        matchesError(Tag.L_PARENTHESIS,"(");
        expression();
        while (matches(Tag.COLON)){
            expression();
        }
        matchesError(Tag.R_PARENTHESIS,")");
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
            printError("Error: Debe ser identificador "+token.getLexeme());
        }
        symbolTables.peek().add(token.getLexeme(), type);
        getToken();
    }

    private void assignment(){
        if (!matches(Tag.EQUAL)){
            printError("Error: Se esperaba operador de asignacion =");
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
            matchesError(Tag.R_PARENTHESIS,")");
        }else if(matches(Tag.CALL)){
            functionCall();
        }else{
            printError("Error: "+token.getLexeme()+" no es factor");
        }
    }

    private void arrayOperator(){
        matchesError(Tag.L_BRACKET,"[");
        expression();
        matchesError(Tag.R_BRACKET,"]");
    }

    private void functionCall(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                printError("Error: No se ha declarado la funcion "+token.getLexeme());
            }else if (symbol.getType() != SymbolTable.Type.FUNCTION){
                printError("Error: Identificador "+token.getLexeme()+" debe ser funcion");
            }
            getToken();
        }
        arguments();
    }

    private void relational(){
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL)){

        }else{
            printError("Error: "+token.getLexeme()+" no es operador relacional");
        }
    }

    private boolean type(){
        if (basicType()){
            this.type = SymbolTable.Type.VARIABLE;
            return true;
        }else if (matches(Tag.ARRAY)){
            arrayOperator();
            if (!basicType()){
                printError("Error: Se esperaba un tipo de dato");
            }
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

    private boolean location(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                printError("Error: No se ha declarado la variable "+token.getLexeme());
            }else if (symbol.getType() == SymbolTable.Type.ARRAY) {
                getToken();
                arrayOperator();
                return true;
            }else if (symbol.getType() != SymbolTable.Type.VARIABLE){
                printError("Error: Identificador "+token.getLexeme()+" debe ser una variable");
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

    private boolean matchesError(Tag tag, String symbol){
        if (is(tag)){
            getToken();
            return true;
        }
        printError("Error: Se esperaba "+symbol);
        return false;
    }

    private void getToken(){
        token = scanner.getToken();
        if (token == null){
            token = new Token(Tag.NULL);
        }
    }

    private void addSymbolTable(){
        symbolTables.push(new SymbolTable(symbolTables.peek()));
    }

    private void removeSymbolTable(){
        symbolTables.pop();
    }

    private void printError(String error){
        ErrorLog.logError(error.concat(" Line: "+(scanner.getLine()-1)));
        Main.close();
    }
}