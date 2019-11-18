package com.uca.parser;

import com.uca.scanner.TokenValue;
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
    private Token auxToken;

    private Stack<SymbolTable> symbolTables = new Stack<>();
    private SymbolTable.Type type;
    private SymbolTable.DataType dataType;
    private int offset;

    private PCodeGenerator pCodeGenerator;

    public Parser(Scanner scanner, PCodeGenerator pCodeGenerator) {
        this.scanner = scanner;
        this.pCodeGenerator = pCodeGenerator;
    }

    public void parse() {
        getToken();
        program();
        printSymbolTable();
        pCodeGenerator.printPCode();
        pCodeGenerator.savePCode();
    }

    private void program() {
        symbolTables.push(new SymbolTable(null));
        int jumpToMain = pCodeGenerator.generateJump();
        declarations();
        functions();
        pCodeGenerator.setJumpLocation(jumpToMain);
        main();
    }

    private void main() {
        pCodeGenerator.generateAllocate(symbolTables.peek().getnVariables());
        while (!scanner.isEndOfFile()) {
            statement();
        }
    }

    private void block() {
        matches(Tag.L_BRACE, "{");
        while (!matches(Tag.R_BRACE)) {
            statement();
        }
    }

    private void declarations() {
        while (type()) {
            declaration();
            matches(Tag.SEMICOLON, ";");
        }
    }

    private void declaration(){
        type();
        if (!matches(Tag.IDENTIFIER)) {
            printError("Error: Debe ser identificador " + lastToken.getLexeme());
        }
        addSymbol(lastToken.getLexeme(), type, dataType, offset);
        offset = 1;
    }

    private boolean type() {
        if (basicType()) {
            this.type = SymbolTable.Type.VARIABLE;
            return true;
        } else if (matches(Tag.ARRAY)) {
            matches(Tag.L_BRACKET, "[");
            if (!basicType()) {
                printError("Error: Se esperaba un tipo de dato");
            }
            matches(Tag.COLON, ",");
            if (!matches(Tag.INTEGER)) {
                printError("Error: Debe ser entero");
            }
            this.offset = ((TokenValue<Integer>)lastToken).getValue();
            matches(Tag.R_BRACKET, "]");
            this.type = SymbolTable.Type.ARRAY;
            return true;
        }
        return false;
    }

    private boolean basicType() {
        if (matches(Tag.INT)) {
            this.dataType = SymbolTable.DataType.INTEGER;
            return true;
        } else if (matches(Tag.DEC)) {
            this.dataType = SymbolTable.DataType.DECIMAL;
            return true;
        } else if (matches(Tag.BOO)) {
            this.dataType = SymbolTable.DataType.BOOLEAN;
            return true;
        } else if (matches(Tag.CHAR)) {
            this.dataType = SymbolTable.DataType.CHARACTER;
            return true;
        } else if (matches(Tag.STR)) {
            this.dataType = SymbolTable.DataType.STRING;
            return true;
        }
        return false;
    }

    private void functions() {
        while (matches(Tag.FUNCTION)) {
            type();
            if (matches(Tag.IDENTIFIER)) {
                addFunction(lastToken.getLexeme(), dataType, pCodeGenerator.getIp());
            }
            addSymbolTable();
            parameters();
            functionBlock();
            removeSymbolTable();
        }
    }

    private void functionBlock() {
        matches(Tag.L_BRACE, "{");
        declarations();
        pCodeGenerator.generateAllocate(symbolTables.peek().getnVariables());
        while (!matches(Tag.RETURN)) {
            statement();
        }
        expression();
        matches(Tag.SEMICOLON, ";");
        matches(Tag.R_BRACE, "}");
        pCodeGenerator.generateReturn();
    }

    private void functionCall() {
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.IDENTIFIER)) {
            Symbol s = getSymbol(lastToken.getLexeme());
            if (s == null) {
                printError("Error: No se ha declarado la funcion " + lastToken.getLexeme());
            } else if (s.getType() != SymbolTable.Type.FUNCTION) {
                printError("Error: Identificador " + lastToken.getLexeme() + " debe ser funcion");
            }
            arguments();
            matches(Tag.R_PARENTHESIS, ")");
            pCodeGenerator.generateCall(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
        } else {
            printError("Error: Call debe ir seguido de un identificador");
        }
    }

    private void parameters() {
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)){
            return;
        }
        declaration();
        while (matches(Tag.COLON)) {
            declaration();
        }
        matches(Tag.R_PARENTHESIS, ")");
    }

    private void arguments() {
        int count=0;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)){
            return;
        }
        expression();
        count++;
        while (matches(Tag.COLON)) {
            expression();
            count++;
        }
        matches(Tag.R_PARENTHESIS, ")");
        pCodeGenerator.generateParams(count);
    }

    private void ifBlock() {
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
        int jumpToElse = pCodeGenerator.generateConditionalJump();
        block();
        int jumpToExit = pCodeGenerator.generateJump();
        pCodeGenerator.setJumpLocation(jumpToElse);
        if (matches(Tag.IFNOT)) {
            block();
        }
        pCodeGenerator.setJumpLocation(jumpToExit);
    }

    private void forBlock() {
        matches(Tag.L_PARENTHESIS, "(");
        assignment();
        int conditionLocation = pCodeGenerator.getIp();
        matches(Tag.SEMICOLON, ";");
        conditions();
        int jumpToExit = pCodeGenerator.generateConditionalJump();
        int jumpToBody = pCodeGenerator.generateJump();
        matches(Tag.SEMICOLON, ";");
        int assignmentLocation = pCodeGenerator.getIp();
        assignment();
        pCodeGenerator.generateJump(conditionLocation);
        matches(Tag.R_PARENTHESIS, ")");
        pCodeGenerator.setJumpLocation(jumpToBody);
        block();
        pCodeGenerator.generateJump(assignmentLocation);
        pCodeGenerator.setJumpLocation(jumpToExit);
    }

    private void whileBlock() {
        int conditionLocation = pCodeGenerator.getIp();
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
        int jumpTpExit = pCodeGenerator.generateConditionalJump();
        block();
        pCodeGenerator.generateJump(conditionLocation);
        pCodeGenerator.setJumpLocation(jumpTpExit);
    }

    private void doWhileBlock() {
        int startLocation = pCodeGenerator.getIp();
        block();
        matches(Tag.WHILE, "while");
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
        pCodeGenerator.generateInverseJump(startLocation);
    }

    private void statement() {
        if (location()) {
            assignment();
            matches(Tag.SEMICOLON, ";");
        } else if (matches(Tag.IF)) {
            ifBlock();
        } else if (matches(Tag.FOR)) {
            forBlock();
        } else if (matches(Tag.WHILE)) {
            whileBlock();
        } else if (matches(Tag.DO)) {
            doWhileBlock();
        } else if (matches(Tag.CALL)) {
            functionCall();
            matches(Tag.SEMICOLON, ";");
        } else {
            printError("Error: No es instruccion " + token.getLexeme() + ".");
        }
    }

    private void assignment() {
        location();
        Symbol s = getSymbol(auxToken.getLexeme());
        if (!matches(Tag.EQUAL)) {
            printError("Error: Se esperaba operador de asignacion =");
        }
        expression();
        if (s.getType() == SymbolTable.Type.ARRAY){
            pCodeGenerator.generateAssignmentOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
        }else {
            pCodeGenerator.generateAssignment(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
        }
    }

    private void conditions() {
        condition();
        while (matches(Tag.AND) || matches(Tag.OR)) {
            Token op = lastToken;
            condition();
            if (op.getTag() == Tag.AND) {
                pCodeGenerator.generateAnd();
            } else {
                pCodeGenerator.generateOr();
            }
        }
    }

    private void condition() {
        expression();
        relational();
        Token op = lastToken;
        expression();
        switch (op.getTag()){
            case EQUAL_EQUAL:
                pCodeGenerator.generateEqual();
                break;
            case NOT_EQUAL:
                pCodeGenerator.generateNotEqual();
                break;
            case LESS_THAN:
                pCodeGenerator.generateLessThan();
                break;
            case LESS_THAN_EQUAL:
                pCodeGenerator.generateLessThanEqual();
                break;
            case GREATER_THAN:
                pCodeGenerator.generateGreaterThan();
                break;
            case GREATER_THAN_EQUAL:
                pCodeGenerator.generateGreaterThanEqual();
                break;
        }
    }

    private void relational() {
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL)) {

        } else{
            printError("Error: " + token.getLexeme() + " no es operador relacional");
        }
    }

    private void expression() {
        term();
        while (matches(Tag.PLUS) || matches(Tag.MINUS)) {
            Token op = lastToken;
            term();
            if (op.getTag() == Tag.PLUS) {
                pCodeGenerator.generateSum();
            } else {
                pCodeGenerator.generateSubtract();
            }
        }
    }

    private void term() {
        Token unary = null;
        if (matches(Tag.MINUS) || matches(Tag.PLUS) || matches(Tag.NOT)){
            unary = lastToken;
        }
        factor();
        while (matches(Tag.MULTIPLICATION) || matches(Tag.DIVISION)) {
            Token op = lastToken;
            factor();
            if (op.getTag() == Tag.MULTIPLICATION) {
                pCodeGenerator.generateMultiplication();
            } else {
                pCodeGenerator.generateDivision();
            }
        }
        if (unary != null){
            switch (unary.getTag()){
                case MINUS:
                    pCodeGenerator.generateNegative();
                    break;
                case PLUS:
                    pCodeGenerator.generatePositive();
                    break;
                case NOT:
                    pCodeGenerator.generateNot();
                    break;
            }
        }
    }

    private void factor() {
        if (matches(Tag.INTEGER)) {
            pCodeGenerator.generateValue(((TokenValue<Integer>) lastToken).getValue());
        } else if (matches(Tag.DECIMAL)) {
            pCodeGenerator.generateValue(((TokenValue<  Double>) lastToken).getValue());
        } else if (matches(Tag.CHARACTER)) {
            pCodeGenerator.generateValue(((TokenValue<Character>) lastToken).getValue());
        } else if (matches(Tag.STRING)) {
            pCodeGenerator.generateValue(((TokenValue<String>) lastToken).getValue());
        } else if (matches(Tag.TRUE) || matches(Tag.FALSE)) {
            pCodeGenerator.generateValue(((TokenValue<Boolean>) lastToken).getValue());
        } else if (location()) {
            Symbol s = symbolTables.peek().get(auxToken.getLexeme());
            if (s.getType() == SymbolTable.Type.ARRAY) {
                pCodeGenerator.generateVariableOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            }else{
                pCodeGenerator.generateVariable(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            }
        } else if (matches(Tag.L_PARENTHESIS)) {
            expression();
            matches(Tag.R_PARENTHESIS, ")");
        } else if (matches(Tag.CALL)) {
            functionCall();
        } else {
            printError("Error: " + token.getLexeme() + " no es factor");
        }
    }

    private boolean location() {
        if (matches(Tag.IDENTIFIER)) {
            Symbol symbol = getSymbol(lastToken.getLexeme());
            Token saveToken = lastToken;
            auxToken = saveToken;
            if (symbol == null) {
                printError("Error: No se ha declarado la variable " + lastToken.getLexeme());
            } else if (symbol.getType() == SymbolTable.Type.ARRAY) {
                arrayOperator();
                auxToken = saveToken;
                return true;
            } else if (symbol.getType() != SymbolTable.Type.VARIABLE) {
                printError("Error: Identificador " + lastToken.getLexeme() + " debe ser una variable");
            }
            return true;
        }
        return false;
    }

    private void arrayOperator() {
        matches(Tag.L_BRACKET, "[");
        expression();
        matches(Tag.R_BRACKET, "]");
    }

    private boolean is(Tag tag) {
        lastToken = token;
        if (token.getTag() == tag) {
            return true;
        }
        return false;
    }

    private boolean matches(Tag tag) {
        if (is(tag)) {
            getToken();
            return true;
        }
        return false;
    }

    private boolean matches(Tag tag, String symbol) {
        if (is(tag)) {
            getToken();
            return true;
        }
        printError("Error: Se esperaba " + symbol);
        return false;
    }

    private void getToken() {
        token = scanner.getToken();
        if (token == null) {
            token = new Token(Tag.NULL);
        }
    }

    private void addSymbol(String lexeme, SymbolTable.Type type, SymbolTable.DataType dataType){
        symbolTables.peek().add(lexeme, type, dataType, 1);
    }

    private void addSymbol(String lexeme, SymbolTable.Type type, SymbolTable.DataType dataType, int offset){
        symbolTables.peek().add(lexeme, type, dataType, offset);
    }

    private void addFunction(String lexeme, SymbolTable.DataType dataType, int address){
        symbolTables.peek().addFunction(lexeme, dataType, address);
    }

    private Symbol getSymbol(String lexeme){
        return symbolTables.peek().get(lexeme);
    }

    private void addSymbolTable() {
        symbolTables.push(new SymbolTable(symbolTables.peek()));
    }

    private void removeSymbolTable() {
        symbolTables.pop();
    }

    private void printError(String error) {
        ErrorLog.logError(error.concat(" Line: " + token.getLine()));
        Main.close();
    }

    private void printSymbolTable() {
        System.out.println();
        System.out.println("Symbol Table:");
        for (SymbolTable st : symbolTables) {
            st.printSymbols();
        }
    }
}