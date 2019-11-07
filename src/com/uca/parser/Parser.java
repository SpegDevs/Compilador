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
    private SymbolTable.DataType dataType;

    private PCodeGenerator pCodeGenerator = new PCodeGenerator();
    private int ip;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
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
        declarations();
        functions();
        main();
    }

    private void declarations() {
        while (type()) {
            declaration();
            while (matches(Tag.COLON)) {
                declaration();
            }
            matches(Tag.SEMICOLON, ";");
        }
    }

    private void declaration() {
        if (!is(Tag.IDENTIFIER)) {
            printError("Error: Debe ser identificador " + token.getLexeme());
        }
        symbolTables.peek().add(token.getLexeme(), type, dataType);
        getToken();
    }

    private boolean type() {
        if (basicType()) {
            this.type = SymbolTable.Type.VARIABLE;
            return true;
        } else if (matches(Tag.ARRAY)) {
            matches(Tag.L_BRACKET, "[");
            if (!matches(Tag.INTEGER)) {
                printError("Error: Debe ser entero");
            }
            matches(Tag.R_BRACKET, "]");
            if (!basicType()) {
                printError("Error: Se esperaba un tipo de dato");
            }
            this.type = SymbolTable.Type.ARRAY;
            return true;
        } else if (matches(Tag.STR)) {
            matches(Tag.L_BRACKET, "[");
            if (!matches(Tag.INTEGER)) {
                printError("Error: Debe ser entero");
            }
            matches(Tag.R_BRACKET, "]");
            this.type = SymbolTable.Type.ARRAY;
            this.dataType = SymbolTable.DataType.STRING;
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
        }
        return false;
    }

    private void functions() {
        while (matches(Tag.FUNCTION)) {
            type();
            if (matches(Tag.IDENTIFIER)) {
                symbolTables.peek().add(lastToken.getLexeme(), SymbolTable.Type.FUNCTION, dataType);
            }
            addSymbolTable();
            parameters();
            functionBlock();
            removeSymbolTable();
        }
    }

    private void main() {
        while (!scanner.isEndOfFile()) {
            statement();
        }
    }

    private void block() {
        addSymbolTable();
        matches(Tag.L_BRACE, "{");
        while (!matches(Tag.R_BRACE)) {
            statement();
        }
        removeSymbolTable();
    }

    private void functionBlock() {
        addSymbolTable();
        matches(Tag.L_BRACE, "{");
        declarations();
        while (!matches(Tag.RETURN)) {
            statement();
        }
        expression();
        matches(Tag.SEMICOLON, ";");
        matches(Tag.R_BRACE, "}");
        removeSymbolTable();
    }

    private void ifBlock() {
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
        block();
        if (matches(Tag.IFNOT)) {
            block();
        }
    }

    private void forBlock() {
        matches(Tag.L_PARENTHESIS, "(");
        location();
        assignment();
        matches(Tag.SEMICOLON, ";");
        conditions();
        matches(Tag.SEMICOLON, ";");
        location();
        assignment();
        matches(Tag.R_PARENTHESIS, ")");
        block();
    }

    private void whileBlock() {
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
        block();
    }

    private void doWhileBlock() {
        block();
        matches(Tag.WHILE, "while");
        matches(Tag.L_PARENTHESIS, "(");
        conditions();
        matches(Tag.R_PARENTHESIS, ")");
    }

    private void statement() {
        if (location()) {
            assignment();
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

    private void parameters() {
        matches(Tag.L_PARENTHESIS, "(");
        type();
        declaration();
        while (matches(Tag.COLON)) {
            type();
            declaration();
        }
        matches(Tag.R_PARENTHESIS, ")");
    }

    private void arguments() {
        matches(Tag.L_PARENTHESIS, "(");
        expression();
        while (matches(Tag.COLON)) {
            expression();
        }
        matches(Tag.R_PARENTHESIS, ")");
    }

    private void conditions() {
        condition();
        while (matches(Tag.AND) || matches(Tag.OR)) {
            condition();
        }
    }

    private void condition() {
        expression();
        relational();
        expression();
    }

    private void assignment() {
        if (!matches(Tag.EQUAL)) {
            printError("Error: Se esperaba operador de asignacion =");
        }
        expression();
        matches(Tag.SEMICOLON, ";");
    }

    private void expression() {
        term();
        while (matches(Tag.PLUS) || matches(Tag.MINUS)) {
            term();
        }
    }

    private void term() {
        factor();
        while (matches(Tag.MULTIPLICATION) || matches(Tag.DIVISION)) {
            factor();
        }
    }

    private void factor() {
        if (matches(Tag.INTEGER) || matches(Tag.DECIMAL) || matches(Tag.STRING) || matches(Tag.CHARACTER) || matches(Tag.TRUE) || matches(Tag.FALSE)) {
        } else if (location()) {
        } else if (matches(Tag.L_PARENTHESIS)) {
            expression();
            matches(Tag.R_PARENTHESIS, ")");
        } else if (matches(Tag.CALL)) {
            functionCall();
        } else {
            printError("Error: " + token.getLexeme() + " no es factor");
        }
    }

    private void arrayOperator() {
        matches(Tag.L_BRACKET, "[");
        expression();
        matches(Tag.R_BRACKET, "]");
    }

    private void functionCall() {
        matches(Tag.L_PARENTHESIS, "(");
        if (is(Tag.IDENTIFIER)) {
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null) {
                printError("Error: No se ha declarado la funcion " + token.getLexeme());
            } else if (symbol.getType() != SymbolTable.Type.FUNCTION) {
                printError("Error: Identificador " + token.getLexeme() + " debe ser funcion");
            }
            getToken();
        } else {
            printError("Error: Call debe ir seguido de un identificador");
        }
        arguments();
        matches(Tag.R_PARENTHESIS, ")");
    }

    private void relational() {
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL)) {

        } else {
            printError("Error: " + token.getLexeme() + " no es operador relacional");
        }
    }

    private boolean location() {
        if (is(Tag.IDENTIFIER)) {
            Symbol symbol = symbolTables.peek().get(token.getLexeme());
            if (symbol == null) {
                printError("Error: No se ha declarado la variable " + token.getLexeme());
            } else if (symbol.getType() == SymbolTable.Type.ARRAY) {
                getToken();
                arrayOperator();
                return true;
            } else if (symbol.getType() != SymbolTable.Type.VARIABLE) {
                printError("Error: Identificador " + token.getLexeme() + " debe ser una variable");
            }
            getToken();
            return true;
        }
        return false;
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