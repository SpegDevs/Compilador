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

    private void declaration() {
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
            this.offset = ((TokenValue<Integer>) lastToken).getValue();
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
            if (!type()){
                printError("Error: Se esperaba un tipo de dato");
            }
            Symbol s = null;
            if (matches(Tag.IDENTIFIER)) {
                s = addFunction(lastToken.getLexeme(), dataType, pCodeGenerator.getIp());
            }
            addSymbolTable();
            int params = parameters();
            if (s != null) {
                s.setParams(params);
            }
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

    private SymbolTable.DataType functionCall() {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.IDENTIFIER)) {
            Symbol s = getSymbol(lastToken.getLexeme());
            if (s == null) {
                printError("Error: No se ha declarado la funcion " + lastToken.getLexeme());
            } else if (s.getType() != SymbolTable.Type.FUNCTION) {
                printError("Error: Identificador " + lastToken.getLexeme() + " debe ser funcion");
            }
            int args = arguments();
            checkFunctionCall(s.getParams(), args);
            pCodeGenerator.generateParams(args);
            matches(Tag.R_PARENTHESIS, ")");
            pCodeGenerator.generateCall(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            type = s.getDataType();
        } else if (builtInFunction()) {
            Token fun = lastToken;
            int args = arguments();
            matches(Tag.R_PARENTHESIS, ")");
            switch (fun.getTag()) {
                case MAX:
                    checkFunctionCall(2, args);
                    pCodeGenerator.generateMax();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case MIN:
                    checkFunctionCall(2, args);
                    pCodeGenerator.generateMin();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case RANDOM:
                    checkFunctionCall(2, args);
                    pCodeGenerator.generateRandom();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case FACTORIAL:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFactorial();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case POW:
                    checkFunctionCall(2, args);
                    pCodeGenerator.generatePow();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case SQRT:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateSqrt();
                    type = SymbolTable.DataType.DECIMAL;
                    break;
                case CEIL:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateCeil();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case FLOOR:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFloor();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case ROUND:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateRound();
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case SUBSTRING:
                    checkFunctionCall(3, args);
                    pCodeGenerator.generateSubstring();
                    type = SymbolTable.DataType.STRING;
                    break;
                case OUT:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateOut();
                    type = SymbolTable.DataType.VOID;
                    break;
                case IN_INT:
                    checkFunctionCall(0, args);
                    pCodeGenerator.generateIn(0);
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case IN_DEC:
                    checkFunctionCall(0, args);
                    pCodeGenerator.generateIn(1);
                    type = SymbolTable.DataType.DECIMAL;
                    break;
                case IN_CHA:
                    checkFunctionCall(0, args);
                    pCodeGenerator.generateIn(2);
                    type = SymbolTable.DataType.CHARACTER;
                    break;
                case IN_STR:
                    checkFunctionCall(0, args);
                    pCodeGenerator.generateIn(3);
                    type = SymbolTable.DataType.STRING;
                    break;
                case IN_BOO:
                    checkFunctionCall(0, args);
                    pCodeGenerator.generateIn(4);
                    type = SymbolTable.DataType.BOOLEAN;
                    break;
                case FILE_WRITE:
                    checkFunctionCall(2, args);
                    pCodeGenerator.generateFileWrite();
                    type = SymbolTable.DataType.VOID;
                    break;
                case FILE_READ_INT:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileRead(0);
                    type = SymbolTable.DataType.INTEGER;
                    break;
                case FILE_READ_DEC:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileRead(1);
                    type = SymbolTable.DataType.DECIMAL;
                    break;
                case FILE_READ_CHA:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileRead(2);
                    type = SymbolTable.DataType.CHARACTER;
                    break;
                case FILE_READ_STR:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileRead(3);
                    type = SymbolTable.DataType.STRING;
                    break;
                case FILE_READ_BOO:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileRead(4);
                    type = SymbolTable.DataType.BOOLEAN;
                    break;
            }
        } else {
            printError("Error: Call debe ir seguido de un identificador");
        }
        return type;
    }

    private int parameters() {
        int count = 0;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)) {
            return 0;
        }
        declaration();
        count++;
        while (matches(Tag.COLON)) {
            declaration();
            count++;
        }
        matches(Tag.R_PARENTHESIS, ")");
        return count;
    }

    private int arguments() {
        int count = 0;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)) {
            return 0;
        }
        expression();
        count++;
        while (matches(Tag.COLON)) {
            expression();
            count++;
        }
        matches(Tag.R_PARENTHESIS, ")");
        return count;
    }

    private boolean builtInFunction() {
        if (matches(Tag.MAX) || matches(Tag.MIN) || matches(Tag.RANDOM) || matches(Tag.FACTORIAL) || matches(Tag.POW) || matches(Tag.SQRT) || matches(Tag.FLOOR) || matches(Tag.CEIL) || matches(Tag.ROUND) || matches(Tag.SUBSTRING)) {
            return true;
        }
        if (matches(Tag.OUT) || matches(Tag.IN_INT) || matches(Tag.IN_DEC) || matches(Tag.IN_CHA) || matches(Tag.IN_STR) || matches(Tag.IN_BOO)) {
            return true;
        }
        if (matches(Tag.FILE_OPEN) || matches(Tag.FILE_WRITE) || matches(Tag.FILE_READ_INT) || matches(Tag.FILE_READ_DEC) || matches(Tag.FILE_READ_CHA) || matches(Tag.FILE_READ_STR) || matches(Tag.FILE_READ_BOO)) {
            return true;
        }
        return false;
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
            printError("Error: No es instruccion " + token.getLexeme());
        }
    }

    private void assignment() {
        location();
        Symbol s = getSymbol(auxToken.getLexeme());
        if (!matches(Tag.EQUAL)) {
            printError("Error: Se esperaba operador de asignacion =");
        }
        SymbolTable.DataType type = expression();
        if (type != s.getDataType()) {
            if (!attemptTypeConversion(s.getDataType(), type)) {
                printError("Error: Se esperaba un valor de tipo " + s.getDataType().toString() + ", se enconto " + type.toString());
            }
        }
        if (s.getType() == SymbolTable.Type.ARRAY) {
            pCodeGenerator.generateAssignmentOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
        } else {
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

    private SymbolTable.DataType condition() {
        SymbolTable.DataType type1 = expression();
        relational();
        Token op = lastToken;
        SymbolTable.DataType type2 = expression();
        if (type1 != type2) {
            if (!attemptTypeConversion(type1, type2)) {
                printError("Error: Operador " + op.getTag() + " esperaba valores del mismo tipo " + type1.toString());
            }
        }
        switch (op.getTag()) {
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
        return SymbolTable.DataType.BOOLEAN;
    }

    private void relational() {
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL)) {

        } else {
            printError("Error: " + token.getLexeme() + " no es operador relacional");
        }
    }

    private SymbolTable.DataType expression() {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        SymbolTable.DataType type2 = SymbolTable.DataType.VOID;
        type = term();
        while (matches(Tag.PLUS) || matches(Tag.MINUS)) {
            Token op = lastToken;
            type2 = term();
            if (op.getTag() == Tag.PLUS) {
                if (type != SymbolTable.DataType.INTEGER && type != SymbolTable.DataType.DECIMAL && type != SymbolTable.DataType.STRING) {
                    printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type.toString());
                }
                if (type2 != SymbolTable.DataType.INTEGER && type2 != SymbolTable.DataType.DECIMAL && type2 != SymbolTable.DataType.STRING) {
                    printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type2.toString());
                }
                if (type == SymbolTable.DataType.STRING || type2 == SymbolTable.DataType.STRING) {
                    type = SymbolTable.DataType.STRING;
                } else if (type == SymbolTable.DataType.DECIMAL || type2 == SymbolTable.DataType.DECIMAL) {
                    type = SymbolTable.DataType.DECIMAL;
                } else {
                    type = SymbolTable.DataType.INTEGER;
                }
                pCodeGenerator.generateSum();
            } else {
                if (type != SymbolTable.DataType.INTEGER && type != SymbolTable.DataType.DECIMAL) {
                    printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type.toString());
                }
                if (type2 != SymbolTable.DataType.INTEGER && type2 != SymbolTable.DataType.DECIMAL) {
                    printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type2.toString());
                }
                if (type == SymbolTable.DataType.DECIMAL || type2 == SymbolTable.DataType.DECIMAL) {
                    type = SymbolTable.DataType.DECIMAL;
                } else {
                    type = SymbolTable.DataType.INTEGER;
                }
                pCodeGenerator.generateSubtract();
            }
        }
        return type;
    }

    private SymbolTable.DataType term() {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        SymbolTable.DataType type2 = SymbolTable.DataType.VOID;
        type = unary();
        while (matches(Tag.MULTIPLICATION) || matches(Tag.DIVISION)) {
            Token op = lastToken;
            type2 = unary();
            if (op.getTag() == Tag.MULTIPLICATION) {
                pCodeGenerator.generateMultiplication();
            } else {
                pCodeGenerator.generateDivision();
            }
            if (type != SymbolTable.DataType.INTEGER && type != SymbolTable.DataType.DECIMAL) {
                printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type.toString());
            }
            if (type2 != SymbolTable.DataType.INTEGER && type2 != SymbolTable.DataType.DECIMAL) {
                printError("Error: Operador " + op.getLexeme() + " no se puede aplicar a " + type2.toString());
            }
            if (type == SymbolTable.DataType.DECIMAL || type2 == SymbolTable.DataType.DECIMAL) {
                type = SymbolTable.DataType.DECIMAL;
            } else {
                type = SymbolTable.DataType.INTEGER;
            }
        }
        return type;
    }

    private SymbolTable.DataType unary() {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        Token op = null;
        if (matches(Tag.MINUS) || matches(Tag.PLUS) || matches(Tag.NOT)) {
            op = lastToken;
        }
        type = factor();
        if (op != null) {
            switch (op.getTag()) {
                case MINUS:
                    if (type != SymbolTable.DataType.INTEGER && type != SymbolTable.DataType.DECIMAL) {
                        printError("Error: Operador unario - no compatible con tipo " + type.toString());
                    }
                    pCodeGenerator.generateNegative();
                    break;
                case PLUS:
                    if (type != SymbolTable.DataType.INTEGER && type != SymbolTable.DataType.DECIMAL) {
                        printError("Error: Operador unario + no compatible con tipo " + type.toString());
                    }
                    pCodeGenerator.generatePositive();
                    break;
                case NOT:
                    if (type != SymbolTable.DataType.BOOLEAN) {
                        printError("Error: Operador unario ! no compatible con tipo " + type.toString());
                    }
                    pCodeGenerator.generateNot();
                    break;
            }
        }
        return type;
    }

    private SymbolTable.DataType factor() {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        if (matches(Tag.INTEGER)) {
            pCodeGenerator.generateValue(((TokenValue<Integer>) lastToken).getValue());
            type = SymbolTable.DataType.INTEGER;
        } else if (matches(Tag.DECIMAL)) {
            pCodeGenerator.generateValue(((TokenValue<Double>) lastToken).getValue());
            type = SymbolTable.DataType.DECIMAL;
        } else if (matches(Tag.CHARACTER)) {
            pCodeGenerator.generateValue(((TokenValue<Character>) lastToken).getValue());
            type = SymbolTable.DataType.CHARACTER;
        } else if (matches(Tag.STRING)) {
            pCodeGenerator.generateValue(((TokenValue<String>) lastToken).getValue());
            type = SymbolTable.DataType.STRING;
        } else if (matches(Tag.TRUE) || matches(Tag.FALSE)) {
            pCodeGenerator.generateValue(((TokenValue<Boolean>) lastToken).getValue());
            type = SymbolTable.DataType.BOOLEAN;
        } else if (location()) {
            Symbol s = symbolTables.peek().get(auxToken.getLexeme());
            if (s.getType() == SymbolTable.Type.ARRAY) {
                pCodeGenerator.generateVariableOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            } else {
                pCodeGenerator.generateVariable(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            }
            type = s.getDataType();
        } else if (matches(Tag.L_PARENTHESIS)) {
            type = expression();
            matches(Tag.R_PARENTHESIS, ")");
        } else if (matches(Tag.CALL)) {
            type = functionCall();
        } else {
            printError("Error: " + token.getLexeme() + " no es factor");
        }
        return type;
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

    private void addSymbol(String lexeme, SymbolTable.Type type, SymbolTable.DataType dataType) {
        symbolTables.peek().add(lexeme, type, dataType, 1);
    }

    private void addSymbol(String lexeme, SymbolTable.Type type, SymbolTable.DataType dataType, int offset) {
        symbolTables.peek().add(lexeme, type, dataType, offset);
    }

    private Symbol addFunction(String lexeme, SymbolTable.DataType dataType, int address) {
        return symbolTables.peek().addFunction(lexeme, dataType, address);
    }

    private Symbol getSymbol(String lexeme) {
        return symbolTables.peek().get(lexeme);
    }

    private void addSymbolTable() {
        symbolTables.push(new SymbolTable(symbolTables.peek()));
    }

    private void removeSymbolTable() {
        symbolTables.pop();
    }


    private void checkFunctionCall(int params, int args) {
        if (args < params) {
            printError("Error: Faltan argumentos, se esperaban " + params);
        }
        if (args > params) {
            printError("Error: Demasiados argumentos, se esperaban " + params);
        }
    }

    private boolean attemptTypeConversion(SymbolTable.DataType target, SymbolTable.DataType type) {
        switch (target) {
            case INTEGER:
                if (type == SymbolTable.DataType.DECIMAL || type == SymbolTable.DataType.CHARACTER) {
                    return true;
                }
                break;
            case DECIMAL:
                if (type == SymbolTable.DataType.INTEGER || type == SymbolTable.DataType.CHARACTER) {
                    return true;
                }
                break;
            case CHARACTER:
                if (type == SymbolTable.DataType.INTEGER) {
                    return true;
                }
                break;
            case STRING:
                if (type == SymbolTable.DataType.CHARACTER) {
                    return true;
                }
                break;
            case BOOLEAN:
                break;
        }
        return false;
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