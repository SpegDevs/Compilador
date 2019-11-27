package com.uca.parser;

import com.uca.scanner.Tag;

public class Sets {

    public enum Struct{
        DECLARATIONS, DECLARATION,
        DATATYPE, BASICTYPE, ARRAY,
        FUNCTIONS, FUNCTION, FUNCTIONBLOCK,PARAMETERS, MOREPARAMETERS,
        STATEMENTS, STATEMENT, ASSIGNMENT, FUNCTIONCALL, ARGUMENTS, MOREARGUMENTS,
        IF, IFNOT, ELSE, WHILE, DOWHILE, FOR,
        CONDITIONS, CONDITION, MORECONDITONS, RELATIONALOP,
        EXPRESSION, MORETERMS, TERM, MOREUNARY, UNARY, FACTOR, LOCATION, ARRAYACCESS
    }

    public static int[] getStabilizationSet(Struct struct){
        int[] set = new int[100];
        switch (struct){
            case DECLARATIONS:
                set[Tag.FUNCTION.ordinal()] = 1;
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                set[Tag.COLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                break;
            case DECLARATION:
                set[Tag.COLON.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                break;
            case FUNCTIONS:
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                break;
            case FUNCTION: case FUNCTIONBLOCK:
                set[Tag.FUNCTION.ordinal()] = 1;
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                break;
            case PARAMETERS: case MOREPARAMETERS: case ARGUMENTS: case MOREARGUMENTS:
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                break;
            case STATEMENTS:
                set[Tag.R_BRACE.ordinal()] = 1;
                break;
            case STATEMENT:
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                set[Tag.R_BRACE.ordinal()] = 1;
                break;
            case ASSIGNMENT:
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                set[Tag.R_BRACE.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                break;
            case FUNCTIONCALL:
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                set[Tag.R_BRACE.ordinal()] = 1;
                set[Tag.MULTIPLICATION.ordinal()] = 1;
                set[Tag.DIVISION.ordinal()] = 1;
                set[Tag.MINUS.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.EQUAL_EQUAL.ordinal()] = 1;
                set[Tag.NOT_EQUAL.ordinal()] = 1;
                set[Tag.LESS_THAN.ordinal()] = 1;
                set[Tag.LESS_THAN_EQUAL.ordinal()] = 1;
                set[Tag.GREATER_THAN.ordinal()] = 1;
                set[Tag.GREATER_THAN_EQUAL.ordinal()] = 1;
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                break;
            case IF: case IFNOT: case WHILE: case DOWHILE: case ELSE: case FOR:
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.IF.ordinal()] = 1;
                set[Tag.IFNOT.ordinal()] = 1;
                set[Tag.ELSE.ordinal()] = 1;
                set[Tag.WHILE.ordinal()] = 1;
                set[Tag.DO.ordinal()] = 1;
                set[Tag.FOR.ordinal()] = 1;
                set[Tag.R_BRACE.ordinal()] = 1;
                break;
            case CONDITIONS: case MORECONDITONS:
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                break;
            case CONDITION:
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                break;
            case RELATIONALOP:
                set[Tag.PLUS.ordinal()] = 1;
                set[Tag.MINUS.ordinal()] = 1;
                set[Tag.NOT.ordinal()] = 1;
                set[Tag.IDENTIFIER.ordinal()] = 1;
                set[Tag.L_PARENTHESIS.ordinal()] = 1;
                set[Tag.CALL.ordinal()] = 1;
                set[Tag.INTEGER.ordinal()] = 1;
                set[Tag.DECIMAL.ordinal()] = 1;
                set[Tag.CHARACTER.ordinal()] = 1;
                set[Tag.STRING.ordinal()] = 1;
                set[Tag.TRUE.ordinal()] = 1;
                set[Tag.FALSE.ordinal()] = 1;
                break;
            case EXPRESSION: case MORETERMS:
                set[Tag.R_BRACE.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.EQUAL_EQUAL.ordinal()] = 1;
                set[Tag.NOT_EQUAL.ordinal()] = 1;
                set[Tag.LESS_THAN.ordinal()] = 1;
                set[Tag.LESS_THAN_EQUAL.ordinal()] = 1;
                set[Tag.GREATER_THAN.ordinal()] = 1;
                set[Tag.GREATER_THAN_EQUAL.ordinal()] = 1;
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                set[Tag.COLON.ordinal()] = 1;
                break;
            case TERM: case MOREUNARY:
                set[Tag.PLUS.ordinal()] = 1;
                set[Tag.MINUS.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.EQUAL_EQUAL.ordinal()] = 1;
                set[Tag.NOT_EQUAL.ordinal()] = 1;
                set[Tag.LESS_THAN.ordinal()] = 1;
                set[Tag.LESS_THAN_EQUAL.ordinal()] = 1;
                set[Tag.GREATER_THAN.ordinal()] = 1;
                set[Tag.GREATER_THAN_EQUAL.ordinal()] = 1;
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                set[Tag.COLON.ordinal()] = 1;
                break;
            case UNARY: case FACTOR:
                set[Tag.MULTIPLICATION.ordinal()] = 1;
                set[Tag.DIVISION.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.EQUAL_EQUAL.ordinal()] = 1;
                set[Tag.NOT_EQUAL.ordinal()] = 1;
                set[Tag.LESS_THAN.ordinal()] = 1;
                set[Tag.LESS_THAN_EQUAL.ordinal()] = 1;
                set[Tag.GREATER_THAN.ordinal()] = 1;
                set[Tag.GREATER_THAN_EQUAL.ordinal()] = 1;
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                set[Tag.COLON.ordinal()] = 1;
                break;
            case LOCATION: case ARRAYACCESS:
                set[Tag.EQUAL.ordinal()] = 1;
                set[Tag.MULTIPLICATION.ordinal()] = 1;
                set[Tag.DIVISION.ordinal()] = 1;
                set[Tag.SEMICOLON.ordinal()] = 1;
                set[Tag.R_PARENTHESIS.ordinal()] = 1;
                set[Tag.EQUAL_EQUAL.ordinal()] = 1;
                set[Tag.NOT_EQUAL.ordinal()] = 1;
                set[Tag.LESS_THAN.ordinal()] = 1;
                set[Tag.LESS_THAN_EQUAL.ordinal()] = 1;
                set[Tag.GREATER_THAN.ordinal()] = 1;
                set[Tag.GREATER_THAN_EQUAL.ordinal()] = 1;
                set[Tag.AND.ordinal()] = 1;
                set[Tag.OR.ordinal()] = 1;
                set[Tag.COLON.ordinal()] = 1;
                break;
        }
        return set;
    }
}
