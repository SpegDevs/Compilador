package com.uca;

import java.util.ArrayList;
import java.util.List;

public class PCodeGenerator {

    private List<PInstruction> pCode = new ArrayList<>();
    private int index;

    public void generate(PInstruction pInstruction){
        pCode.add(pInstruction);
        index++;
    }

    private void printPCode(){
        for (PInstruction p:pCode){
            System.out.println(p.getInstruction().toString()+" "+p.getLevel()+" "+p.getAddress());
        }
    }

    private void savePCode(){
        FileManager file = new FileManager("code.p");
        file.createFile();
        file.openFile();
        file.clearFile();
        for (PInstruction p:pCode){
            file.writeLine(p.getInstruction().toString()+" "+p.getLevel()+" "+p.getAddress());
        }
        file.clearFile();
    }
}
