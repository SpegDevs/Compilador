package com.uca.pcode;

public class PInstruction {

    private PCode instruction;
    private int level;
    private int address;

    public PInstruction(PCode instruction, int level, int address){
        this.instruction = instruction;
        this.level = level;
        this.address = address;
    }

    public PCode getInstruction() {
        return instruction;
    }

    public void setInstruction(PCode instruction) {
        this.instruction = instruction;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}
