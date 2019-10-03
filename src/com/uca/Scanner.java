package com.uca;

public class Scanner {

    private FileManager file;

    public void scan(String fileName){
        file = new FileManager();
        file.openFile(fileName);
        while (true){
            //getToken();
            System.out.print(getChar());
        }
    }

    public void getToken(){

    }

    public char getChar(){
        if (file.isEndOfFile()){
            file.closeFile();
            System.out.println("Analisis lexicografico finalizado.");
            System.exit(0);
        }
        return file.getNextChar();
    }
}
