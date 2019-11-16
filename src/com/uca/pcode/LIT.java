package com.uca.pcode;

public class LIT<DataType> extends PInstruction{

    private DataType value;
    private int type;

    public LIT(int type, DataType value) {
        super(PCode.LIT);
        this.type = type;
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
        return getInstruction().toString()+" "+type+" "+value;
    }
}
