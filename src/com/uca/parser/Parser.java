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
    private int offset = 1;
    private boolean errors = false;

    private PCodeGenerator pCodeGenerator;

    public Parser(Scanner scanner, PCodeGenerator pCodeGenerator) {
        this.scanner = scanner;
        this.pCodeGenerator = pCodeGenerator;
    }

    public void parse() {
        getToken();
        try {
            program();
        } catch (ParserException e) {
            Main.close();
        }
        printSymbolTable();
        pCodeGenerator.printPCode();
        if (!errors){
            pCodeGenerator.savePCode();
        }else{
            System.out.println("Hay errores de compilacion");
        }
    }

    private void program() throws ParserException{
        symbolTables.push(new SymbolTable(null));
        int jumpToMain = pCodeGenerator.generateJump();
        try {
            declarations();
        } catch (ParserException e){
            stabilize(Sets.Struct.DECLARATIONS);
        }
        try {
            functions();
        } catch (ParserException e){
            stabilize(Sets.Struct.FUNCTIONS);
        }
        pCodeGenerator.setJumpLocation(jumpToMain);
        try{
            main();
        } catch (ParserException e){
            stabilize(Sets.Struct.STATEMENTS);
        }
    }

    private void main() throws ParserException {
        pCodeGenerator.generateAllocate(symbolTables.peek().getnVariables());
        while (!scanner.isEndOfFile()) {
            try {
                statement();
            } catch (ParserException e){
                stabilize(Sets.Struct.STATEMENT);
            }
        }
    }

    private void block() throws ParserException {
        matches(Tag.L_BRACE, "{");
        while (!matches(Tag.R_BRACE) && !scanner.isEndOfFile()) {
            try {
                statement();
            } catch (ParserException e){
                stabilize(Sets.Struct.STATEMENT);
            }
        }
    }

    private void declarations() throws ParserException {
        while (type() && !scanner.isEndOfFile()) {
            try {
                declaration();
            } catch (ParserException e){
                stabilize(Sets.Struct.DECLARATION);
            }
            matches(Tag.SEMICOLON, ";");
        }
    }

    private void declaration() throws ParserException {
        type();
        if (!matches(Tag.IDENTIFIER)) {
            printError("Error: Debe ser identificador " + lastToken.getLexeme());
        }
        addSymbol(lastToken.getLexeme(), type, dataType, offset);
        offset = 1;
    }

    private boolean type() throws ParserException {
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

    private void functions() throws ParserException {
        while (matches(Tag.FUNCTION) && !scanner.isEndOfFile()) {
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
            try {
                functionBlock();
            } catch (ParserException e){
                stabilize(Sets.Struct.FUNCTIONBLOCK);
            }
            removeSymbolTable();
        }
    }

    private void functionBlock() throws ParserException {
        matches(Tag.L_BRACE, "{");
        try {
            declarations();
        } catch (ParserException e){
            stabilize(Sets.Struct.DECLARATIONS);
        }
        pCodeGenerator.generateAllocate(symbolTables.peek().getnVariables());
        while (!matches(Tag.RETURN) && !scanner.isEndOfFile()) {
            try {
                statement();
            } catch (ParserException e){
                stabilize(Sets.Struct.STATEMENT);
            }
        }
        try {
            expression();
        } catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
        matches(Tag.SEMICOLON, ";");
        matches(Tag.R_BRACE, "}");
        pCodeGenerator.generateReturn();
    }

    private SymbolTable.DataType functionCall() throws ParserException {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.IDENTIFIER)) {
            Symbol s = getSymbol(lastToken.getLexeme());
            if (s == null) {
                printError("Error: No se ha declarado la funcion " + lastToken.getLexeme());
            } else if (s.getType() != SymbolTable.Type.FUNCTION) {
                printError("Error: Identificador " + lastToken.getLexeme() + " debe ser funcion");
            }
            int args = 0;
            try{
                args = arguments();
            }catch (ParserException e){
                stabilize(Sets.Struct.ARGUMENTS);
            }
            if (s != null) {
                checkFunctionCall(s.getParams(), args);
                pCodeGenerator.generateParams(args);
                pCodeGenerator.generateCall(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
                type = s.getDataType();
            }
            matches(Tag.R_PARENTHESIS, ")");
        } else if (builtInFunction()) {
            Token fun = lastToken;
            int args = 0;
            try{
                args = arguments();
            }catch (ParserException e){
                stabilize(Sets.Struct.ARGUMENTS);
            }
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
                case FILE_CLEAR:
                    checkFunctionCall(1, args);
                    pCodeGenerator.generateFileClear();
                    type = SymbolTable.DataType.VOID;
                    break;
            }
        } else {
            printError("Error: Call debe ir seguido de un identificador");
            throw new ParserException();
        }
        return type;
    }

    private int parameters() throws ParserException {
        int count = 0;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)) {
            return 0;
        }
        try {
            declaration();
            getSymbol(lastToken.getLexeme()).setInitialized(true);
        } catch (ParserException e){
            stabilize(Sets.Struct.DECLARATION);
        }
        count++;
        while (matches(Tag.COLON) && !scanner.isEndOfFile()) {
            try {
                declaration();
                getSymbol(lastToken.getLexeme()).setInitialized(true);
            } catch (ParserException e){
                stabilize(Sets.Struct.DECLARATION);
            }
            count++;
        }
        matches(Tag.R_PARENTHESIS, ")");
        return count;
    }

    private int arguments() throws ParserException {
        int count = 0;
        matches(Tag.L_PARENTHESIS, "(");
        if (matches(Tag.R_PARENTHESIS)) {
            return 0;
        }
        try {
            expression();
        } catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
        count++;
        while (matches(Tag.COLON) && !scanner.isEndOfFile()) {
            try {
                expression();
            } catch (ParserException e){
                stabilize(Sets.Struct.EXPRESSION);
            }
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
        if (matches(Tag.FILE_OPEN) || matches(Tag.FILE_WRITE) || matches(Tag.FILE_READ_INT) || matches(Tag.FILE_READ_DEC) || matches(Tag.FILE_READ_CHA) || matches(Tag.FILE_READ_STR) || matches(Tag.FILE_READ_BOO) || matches(Tag.FILE_CLEAR)) {
            return true;
        }
        return false;
    }

    private void ifBlock() throws ParserException{
        matches(Tag.L_PARENTHESIS, "(");
        try {
            conditions();
        }catch (ParserException e){
            stabilize(Sets.Struct.CONDITIONS);
        }
        matches(Tag.R_PARENTHESIS, ")");
        int jumpToElse = pCodeGenerator.generateConditionalJump();
        block();
        int jumpToExit = pCodeGenerator.generateJump();
        pCodeGenerator.setJumpLocation(jumpToElse);
        try {
            if (matches(Tag.ELSE)) {
                block();
            }
        } catch (ParserException e){
            stabilize(Sets.Struct.ELSE);
        }
        pCodeGenerator.setJumpLocation(jumpToExit);
    }

    private void ifnotBlock() throws ParserException {
        matches(Tag.L_PARENTHESIS, "(");
        try {
            conditions();
        }catch (ParserException e){
            stabilize(Sets.Struct.CONDITIONS);
        }
        matches(Tag.R_PARENTHESIS, ")");
        int jumpToElse = pCodeGenerator.generateInverseJump();
        block();
        int jumpToExit = pCodeGenerator.generateJump();
        pCodeGenerator.setJumpLocation(jumpToElse);
        try {
            if (matches(Tag.ELSE)) {
                block();
            }
        } catch (ParserException e){
            stabilize(Sets.Struct.ELSE);
        }
        pCodeGenerator.setJumpLocation(jumpToExit);
    }

    private void forBlock() throws ParserException {
        matches(Tag.L_PARENTHESIS, "(");
        try {
            assignment();
        }catch (ParserException e){
            stabilize(Sets.Struct.ASSIGNMENT);
        }
        int conditionLocation = pCodeGenerator.getIp();
        matches(Tag.SEMICOLON, ";");
        try {
            conditions();
        }catch (ParserException e){
            stabilize(Sets.Struct.CONDITIONS);
        }
        int jumpToExit = pCodeGenerator.generateConditionalJump();
        int jumpToBody = pCodeGenerator.generateJump();
        matches(Tag.SEMICOLON, ";");
        int assignmentLocation = pCodeGenerator.getIp();
        try {
            assignment();
        }catch (ParserException e){
            stabilize(Sets.Struct.ASSIGNMENT);
        }
        pCodeGenerator.generateJump(conditionLocation);
        matches(Tag.R_PARENTHESIS, ")");
        pCodeGenerator.setJumpLocation(jumpToBody);
        block();
        pCodeGenerator.generateJump(assignmentLocation);
        pCodeGenerator.setJumpLocation(jumpToExit);
    }

    private void whileBlock() throws ParserException {
        int conditionLocation = pCodeGenerator.getIp();
        matches(Tag.L_PARENTHESIS, "(");
        try {
            conditions();
        }catch (ParserException e){
            stabilize(Sets.Struct.CONDITIONS);
        }
        matches(Tag.R_PARENTHESIS, ")");
        int jumpTpExit = pCodeGenerator.generateConditionalJump();
        block();
        pCodeGenerator.generateJump(conditionLocation);
        pCodeGenerator.setJumpLocation(jumpTpExit);
    }

    private void doWhileBlock() throws ParserException {
        int startLocation = pCodeGenerator.getIp();
        block();
        matches(Tag.WHILE, "while");
        matches(Tag.L_PARENTHESIS, "(");
        try {
            conditions();
        }catch (ParserException e){
            stabilize(Sets.Struct.CONDITIONS);
        }
        matches(Tag.R_PARENTHESIS, ")");
        pCodeGenerator.generateInverseJump(startLocation);
    }

    private void statement() throws ParserException{
        if (location()) {
            try {
                assignment();
                matches(Tag.SEMICOLON, ";");
            }catch (ParserException e){
                stabilize(Sets.Struct.ASSIGNMENT);
            }
        } else if (matches(Tag.IF)) {
            try {
                ifBlock();
            }catch (ParserException e){
                stabilize(Sets.Struct.IF);
            }
        } else if (matches(Tag.IFNOT)) {
            try {
                ifnotBlock();
            }catch (ParserException e){
                stabilize(Sets.Struct.IFNOT);
            }
        } else if (matches(Tag.FOR)) {
            try {
                forBlock();
            }catch (ParserException e){
                stabilize(Sets.Struct.FOR);
            }
        } else if (matches(Tag.WHILE)) {
            try {
                whileBlock();
            }catch (ParserException e){
                stabilize(Sets.Struct.WHILE);
            }
        } else if (matches(Tag.DO)) {
            try {
                doWhileBlock();
            }catch (ParserException e){
                stabilize(Sets.Struct.DOWHILE);
            }
        } else if (matches(Tag.CALL)) {
            try {
                functionCall();
                matches(Tag.SEMICOLON, ";");
            }catch (ParserException e){
                stabilize(Sets.Struct.FUNCTIONCALL);
            }
        } else {
            printError("Error: No es instruccion " + token.getLexeme());
            getToken();
            throw new ParserException();
        }
    }

    private void assignment() throws ParserException{
        location();
        Symbol s = getSymbol(auxToken.getLexeme());
        if (!matches(Tag.EQUAL)) {
            printError("Error: Se esperaba operador de asignacion =");
            throw new ParserException();
        }
        SymbolTable.DataType type = null;
        try {
            type = expression();
        }catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
        if (s != null) {
            if (type != s.getDataType()) {
                if (!attemptTypeConversion(s.getDataType(), type)) {
                    printError("Error: Se esperaba un valor de tipo " + s.getDataType().toString() + ", se enconto " + type.toString());
                }
            }
            if (s.getType() == SymbolTable.Type.ARRAY) {
                pCodeGenerator.generateAssignmentOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
            } else {
                pCodeGenerator.generateAssignment(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
                s.setInitialized(true);
            }
        }
    }

    private void conditions() throws ParserException{
        try {
            condition();
        }catch (ParserException e) {
            stabilize(Sets.Struct.CONDITION);
        }
        while (matches(Tag.AND) || matches(Tag.OR) && !scanner.isEndOfFile()) {
            Token op = lastToken;
            try {
                condition();
            }catch (ParserException e){
                stabilize(Sets.Struct.CONDITION);
            }
            if (op.getTag() == Tag.AND) {
                pCodeGenerator.generateAnd();
            } else {
                pCodeGenerator.generateOr();
            }
        }
    }

    private SymbolTable.DataType condition() throws ParserException{
        SymbolTable.DataType type1 = null;
        try {
             type1 = expression();
        } catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
        try {
            relational();
        } catch (ParserException e){
            stabilize(Sets.Struct.RELATIONALOP);
        }
        Token op = lastToken;
        SymbolTable.DataType type2 = null;
        try {
            type2 = expression();
        } catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
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

    private void relational() throws ParserException{
        if (matches(Tag.EQUAL_EQUAL) || matches(Tag.NOT_EQUAL) || matches(Tag.LESS_THAN) || matches(Tag.LESS_THAN_EQUAL) || matches(Tag.GREATER_THAN) || matches(Tag.GREATER_THAN_EQUAL)) {

        } else {
            printError("Error: " + token.getLexeme() + " no es operador relacional");
            throw new ParserException();
        }
    }

    private SymbolTable.DataType expression() throws ParserException{
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        SymbolTable.DataType type2 = SymbolTable.DataType.VOID;
        try {
            type = term();
        } catch (ParserException e){
            stabilize(Sets.Struct.TERM);
        }
        while (matches(Tag.PLUS) || matches(Tag.MINUS) && !scanner.isEndOfFile()) {
            Token op = lastToken;
            try {
                type2 = term();
            } catch (ParserException e){
                stabilize(Sets.Struct.TERM);
            }
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

    private SymbolTable.DataType term() throws ParserException{
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        SymbolTable.DataType type2 = SymbolTable.DataType.VOID;
        try {
            type = unary();
        } catch (ParserException e){
            stabilize(Sets.Struct.UNARY);
        }
        while (matches(Tag.MULTIPLICATION) || matches(Tag.DIVISION) && !scanner.isEndOfFile()) {
            Token op = lastToken;
            try {
                type2 = unary();
            } catch (ParserException e){
                stabilize(Sets.Struct.UNARY);
            }
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

    private SymbolTable.DataType unary() throws ParserException {
        SymbolTable.DataType type = SymbolTable.DataType.VOID;
        Token op = null;
        if (matches(Tag.MINUS) || matches(Tag.PLUS) || matches(Tag.NOT)) {
            op = lastToken;
        }
        try {
            type = factor();
        } catch (ParserException e){
            stabilize(Sets.Struct.FACTOR);
        }
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

    private SymbolTable.DataType factor() throws ParserException {
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
            if (s != null) {
                if (s.getType() == SymbolTable.Type.ARRAY) {
                    pCodeGenerator.generateVariableOffset(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
                } else {
                    if (s.isInitialized()) {
                        pCodeGenerator.generateVariable(symbolTables.peek().getLevel() - s.getLevel(), s.getAddress());
                    } else {
                        printError("Error: No se ha inicializado la variable " + s.getName());
                    }
                }
                type = s.getDataType();
            }
        } else if (matches(Tag.L_PARENTHESIS)) {
            try {
                type = expression();
            } catch (ParserException e){
                stabilize(Sets.Struct.EXPRESSION);
            }
            matches(Tag.R_PARENTHESIS, ")");
        } else if (matches(Tag.CALL)) {
            try {
                type = functionCall();
            } catch (ParserException e){
                stabilize(Sets.Struct.FUNCTIONCALL);
            }
        } else {
            printError("Error: " + token.getLexeme() + " no es factor");
        }
        return type;
    }

    private boolean location() throws ParserException {
        if (matches(Tag.IDENTIFIER)) {
            Symbol symbol = getSymbol(lastToken.getLexeme());
            Token saveToken = lastToken;
            auxToken = saveToken;
            if (symbol == null) {
                printError("Error: No se ha declarado la variable " + lastToken.getLexeme());
            } else if (symbol.getType() == SymbolTable.Type.ARRAY) {
                try {
                    arrayOperator();
                }catch(ParserException e){
                    stabilize(Sets.Struct.ARRAYACCESS);
                }
                auxToken = saveToken;
                return true;
            } else if (symbol.getType() != SymbolTable.Type.VARIABLE) {
                printError("Error: Identificador " + lastToken.getLexeme() + " debe ser una variable");
            }
            return true;
        }
        return false;
    }

    private void arrayOperator() throws ParserException {
        matches(Tag.L_BRACKET, "[");
        try {
            expression();
        } catch (ParserException e){
            stabilize(Sets.Struct.EXPRESSION);
        }
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

    private boolean matches(Tag tag, String symbol) throws ParserException{
        if (is(tag)) {
            getToken();
            return true;
        }
        printError("Error: Se esperaba " + symbol);
        throw new ParserException();
        //return false;
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
                    pCodeGenerator.generateCastToInt();
                    return true;
                }
                break;
            case DECIMAL:
                if (type == SymbolTable.DataType.INTEGER) {
                    pCodeGenerator.generateCastToDec();
                    return true;
                }
                break;
            case CHARACTER:
                break;
            case STRING:
                if (type == SymbolTable.DataType.CHARACTER) {
                    pCodeGenerator.generateCastToStr();
                    return true;
                }
                break;
            case BOOLEAN:
                break;
        }
        return false;
    }

    private void stabilize(Sets.Struct struct){
        int[] set = Sets.getStabilizationSet(struct);
        while (set[token.getTag().ordinal()] != 1 && !scanner.isEndOfFile()){
            getToken();
            if (token.getTag() == Tag.NULL){
                break;
            }
        }
    }

    private void printError(String error) {
        if (token.getTag() != Tag.NULL) {
            ErrorLog.logError(error.concat(" Line: " + token.getLine()));
        }
        errors = true;
        //Main.close();
    }

    private void printSymbolTable() {
        System.out.println();
        System.out.println("Symbol Table:");
        for (SymbolTable st : symbolTables) {
            st.printSymbols();
        }
    }
}