package com.uca.scanner;

public class TokenValue<DataType> extends Token{

    private DataType value;

    public TokenValue(Tag tag, DataType value){
        super(tag);
        this.value = value;
    }

    public TokenValue(Tag tag, String lexeme, DataType value) {
        super(tag, lexeme);
        this.value = value;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        this.value = value;
    }
}
