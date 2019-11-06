package com.uca.parser;

import com.uca.pcode.PCode;
import com.uca.pcode.PInstruction;
import com.uca.tools.ErrorLog;
import com.uca.Main;
import com.uca.pcode.PCodeGenerator;
import com.uca.scanner.Scanner;
import com.uca.scanner.Tag;
import com.uca.scanner.Token;

import java.util.Stack;

public class Parser {

    private Scanner scanner;
    private Token token;
    private Token lastToken;
    private Stack<SymbolTable> symbolTables = new Stack<>();
    private SymbolTable.Type type;
    private int level;
    private PCodeGenerator pCodeGenerator = new PCodeGenerator();

    public Parser(Scanner scanner){
        this.scanner = scanner;
    }

    public void parse(){
        getToken();
        program();
        pCodeGenerator.printPCode();
        pCodeGenerator.savePCode();
    }

    private void program(){
        symbolTables.push(new SymbolTable(null));
        globalDeclarations();
        functions();
        main();
    }

    private void globalDeclarations(){
        while (type()){
            declarations();
            matches(Tag.SEMICOLON,";");
        }
    }

    private void functions(){
        while (matches(Tag.FUNCTION)){
            type();
            if (matches(Tag.IDENTIFIER)){
                symbolTables.peek().add(lastToken.getLexeme(), SymbolTable.Type.FUNCTION);
            }
            addSymbolTable();
            parameters();
            functionBlock();
            removeSymbolTable();
        }
    }

    private void main(){
        while (!scanner.isEndOfFile()){
            statement();
        }
    }

    private void block(){
        addSymbolTable();
        matches(Tag.L_BRACE,"{");
        while (!matches(Tag.R_BRACE)){
            statement();
        }
        removeSymbolTable();
    }

    private void functionBlock(){
        addSymbolTable();
        matches(Tag.L_BRACE,"{");
        while (!matches(Tag.RETURN)){
            statement();
        }
        expression();
        matches(Tag.SEMICOLON,";");
        matches(Tag.R_BRACE,"}");
        removeSymbolTable();
    }

    private void statement(){
        if (type()){
            declarations();
            matches(Tag.SEMICOLON,";");
        }else if (location()){
            assignment();
            matches(Tag.SEMICOLON,";");
        }else if (matches(Tag.IF)){
            matches(Tag.L_PARENTHESIS,"(");
            conditions();
            matches(Tag.R_PARENTHESIS,")");
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.IFNOT)){
            matches(Tag.L_PARENTHESIS,"(");
            conditions();
            matches(Tag.R_PARENTHESIS,")");
            block();
            if (matches(Tag.ELSE)){
                block();
            }
        }else if (matches(Tag.FOR)){
            matches(Tag.L_PARENTHESIS,"(");
            location();
            assignment();
            matches(Tag.SEMICOLON,";");
            conditions();
            matches(Tag.SEMICOLON,";");
            location();
            assignment();
            matches(Tag.R_PARENTHESIS,")");
            block();
        }else if (matches(Tag.WHILE)){
            matches(Tag.L_PARENTHESIS,"(");
            conditions();
            matches(Tag.R_PARENTHESIS,")");
            block();
        }else if (matches(Tag.DO)){
            block();
            matches(Tag.WHILE,"while");
            matches(Tag.L_PARENTHESIS,"(");
            conditions();
            matches(Tag.R_PARENTHESIS,")");
        }else if (matches(Tag.CALL)){
            functionCall();
            matches(Tag.SEMICOLON,";");
        }else{
            printError("Error: No es instruccion "+token.getLexeme()+".");
        }
    }

    private void parameters(){
        matches(Tag.L_PARENTHESIS,"(");
        type();
        declaration();
        while (matches(Tag.COLON)){
            type();
            declaration();
        }
        matches(Tag.R_PARENTHESIS,")");
    }

    private void arguments(){
        matches(Tag.L_PARENTHESIS,"(");
        expression();
        while (matches(Tag.COLON)){
            expression();
        }
        matches(Tag.R_PARENTHESIS,")");
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
        pCodeGenerator.generate(new PInstruction(PCode.INS, level, 0));
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
            matches(Tag.R_PARENTHESIS,")");
        }else if(matches(Tag.CALL)){
            functionCall();
        }else{
            printError("Error: "+token.getLexeme()+" no es factor");
        }
    }

    private void arrayOperator(){
        matches(Tag.L_BRACKET,"[");
        expression();
        matches(Tag.R_BRACKET,"]");
    }

    private void functionCall(){
        if (is(Tag.IDENTIFIER)){
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null){
                printError("Error: No se ha declarado la funcion "+token.getLexeme());
            }else if (symbol.getType() != SymbolTable.Type.FUNCTION){
                printError("Error: Identificador "+token.getLexeme()+" debe ser funcion");
            }
            pCodeGenerator.generate(new PInstruction(PCode.LLA, level-symbol.getLevel(), symbol.getAddress()));
            getToken();
        }else{
            printError("Error: Call debe ir seguido de un identificador");
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
        lastToken = token;
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

    private boolean matches(Tag tag, String symbol){
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
        level++;
    }

    private void removeSymbolTable(){
        symbolTables.pop();
        level--;
    }

    private void printError(String error){
        ErrorLog.logError(error.concat(" Line: "+(scanner.getLine()-1)));
        Main.close();
    }
}