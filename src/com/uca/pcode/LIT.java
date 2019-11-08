package com.uca.pcode;

public class LIT<DataType> extends PInstruction{

    private DataType value;

    public LIT(DataType value) {
        super(PCode.LIT);
        this.value = value;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return getInstruction().toString()+" 0 "+value;
    }
}
