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

    public void generateAllocate(int n){
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

    public void generateAssignmentOffset(int level, int address){
        generate(new PInstruction(PCode.ALO, level, address));
    }

    public void generateVariableOffset(int level, int address){
        generate(new PInstruction(PCode.CAO, level, address));
    }

    public void generateCall(int level, int address){
        generate(new PInstruction(PCode.LLA, level, address));
    }

    public void generateParams(int count){
        generate(new PInstruction(PCode.PAR, 0, count));
    }

    public void generateReturn(){
        generate(new PInstruction(PCode.RET, 0, 0));
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

    public void generateEqual(){
        generate(new PInstruction(PCode.OPR, 0, 7));
    }

    public void generateNotEqual(){
        generate(new PInstruction(PCode.OPR, 0, 8));
    }

    public void generateLessThan(){
        generate(new PInstruction(PCode.OPR, 0, 9));
    }

    public void generateLessThanEqual(){
        generate(new PInstruction(PCode.OPR, 0, 10));
    }

    public void generateGreaterThan(){
        generate(new PInstruction(PCode.OPR, 0, 11));
    }

    public void generateGreaterThanEqual(){
        generate(new PInstruction(PCode.OPR, 0, 12));
    }

    public void generateAnd(){
        generate(new PInstruction(PCode.OPR, 0, 13));
    }

    public void generateOr(){
        generate(new PInstruction(PCode.OPR, 0, 14));
    }

    public void generateNot(){
        generate(new PInstruction(PCode.OPR, 0, 15));
    }

    public void generatePositive(){
        generate(new PInstruction(PCode.OPR, 0, 16));
    }

    public void generateNegative(){
        generate(new PInstruction(PCode.OPR, 0, 17));
    }

    public int generateConditionalJump(){
        generate(new PInstruction(PCode.SAC, 0, 0));
        return ip;
    }

    public void generateConditionalJump(int location){
        generate(new PInstruction(PCode.SAC, 0, location));
    }

    public int generateInverseJump(){
        generate(new PInstruction(PCode.SAI, 0, 0));
        return ip;
    }

    public void generateInverseJump(int location){
        generate(new PInstruction(PCode.SAI, 0, location));
    }


    public int generateJump(){
        generate(new PInstruction(PCode.SAL, 0, 0));
        return ip;
    }

    public void generateJump(int location){
        generate(new PInstruction(PCode.SAL, 0, location));
    }

    public void setJumpLocation(int index){
        this.pCode.get(index-1).setAddress(ip);
    }

    public int getIp(){
        return ip;
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
