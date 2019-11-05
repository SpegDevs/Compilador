package com.uca.pcode;

import com.uca.tools.FileManager;

import java.util.ArrayList;
import java.util.List;

public class PCodeGenerator {

    private List<PInstruction> pCode = new ArrayList<>();
    private int index;

    public void generate(PInstruction pInstruction){
        pCode.add(pInstruction);
        index++;
    }

    public void printPCode(){
        for (PInstruction p:pCode){
            System.out.println(p.getInstruction().toString()+" "+p.getLevel()+" "+p.getAddress());
        }
    }

    public void savePCode(){
        FileManager file = new FileManager("output/code.p");
        file.createFile();
        file.openFile();
        file.clearFile();
        for (PInstruction p:pCode){
            file.writeLine(p.getInstruction().toString()+" "+p.getLevel()+" "+p.getAddress());
        }
        file.clearFile();
    }
}
