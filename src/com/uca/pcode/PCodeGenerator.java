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

    public void generateINS(int n){
        generate(new PInstruction(PCode.INS, 0, n));
    }

    public void generateValue(int value){
        generate(new LIT<Integer>(0,value));
    }

    public void generateValue(double value){
        generate(new LIT<Double>(1,value));
    }

    public void generateValue(char value){
        generate(new LIT<Character>(2,value));
    }

    public void generateValue(String value){
        generate(new LIT<String>(3,value));
    }

    public void generateValue(boolean value){
        generate(new LIT<Boolean>(4,value));
    }

    public void generateAssignment(int level, int address){
        generate(new PInstruction(PCode.ALM, level, address));
    }

    public void generateVariable(int level, int address){
        generate(new PInstruction(PCode.CAR, level, address));
    }

    public void generateSum(){
        generate(new PInstruction(PCode.OPR, 0, 3));
    }

    public void generateSubtract(){
        generate(new PInstruction(PCode.OPR, 0, 4));
    }

    public void generateMultiplication(){
        generate(new PInstruction(PCode.OPR, 0, 5));
    }

    public void generateDivision(){
        generate(new PInstruction(PCode.OPR, 0, 6));
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
