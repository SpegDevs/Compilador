package com.uca.pcode;

import com.uca.tools.FileManager;

import java.util.ArrayList;
import java.util.List;

public class PCodeGenerator {

    private List<PInstruction> pCode = new ArrayList<>();
    private int ip;
    private String fileName;

    public PCodeGenerator(String fileName){
        this.fileName = fileName.split("\\.")[0];
    }

    public void generate(PInstruction pInstruction){
        pCode.add(pInstruction);
        ip++;
    }

    public void generateValue(int value){
        generate(new LIT<Integer>(value));
    }

    public void generateValue(double value){
        generate(new LIT<Double>(value));
    }

    public void generateValue(char value){
        generate(new LIT<Character>(value));
    }

    public void generateValue(String value){
        generate(new LIT<String>(value));
    }

    public void generateValue(boolean value){
        generate(new LIT<Boolean>(value));
    }

    public void generateAssignment(int level, int address){
        generate(new PInstruction(PCode.ALM, level, address));
    }

    public void generateVariable(int level, int address){
        generate(new PInstruction(PCode.CAR, level, address));
    }

    public void printPCode(){
        System.out.println();
        System.out.println("P Code:");
        for (PInstruction p:pCode){
            System.out.println(p.toString());
        }
    }

    public void savePCode(){
        FileManager file = new FileManager("output/"+fileName+".p");
        file.createFile();
        file.clearFile();
        for (PInstruction p:pCode){
            file.writeLine(p.toString());
        }
        file.closeFile();
    }
}
