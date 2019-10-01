package com.uca;

public class Scanner {

    public void scan(String fileName){
        FileManager fileManager = new FileManager();
        fileManager.openFile(fileName);
        char c;
        while (!fileManager.hasNextChar()){
            c = fileManager.getNextChar();
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t'){
                System.out.print(c);
            }
        }
        fileManager.closeFile();
    }
}
