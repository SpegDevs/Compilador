package com.uca.tools;

public class ErrorLog {

    private static FileManager file;

    public static void init(){
        file = new FileManager("output/errors.txt");
        file.createFile();
        file.clearFile();
    }

    public static void logError(String error){
        System.out.println(error);
        file.writeLine(error);
    }

    public static void close(){
        file.closeFile();
    }
}
