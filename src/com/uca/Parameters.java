package com.uca;

public class Parameters {

    public static final int MAX_RESERVED_WORDS = 3;
    public static int MAX_LINE_LENGTH = 1000;
    public static int MAX_DIGITS = 5;
    public static int MAX_IDENTIFIER_LENGTH = 10;

    public static void loadFromFile(String filename){
        FileManager file = new FileManager();
        if (!file.fileExists(filename)) {
            file.createFile(filename);
            file.writeLine("MAX_LINE_LENGTH;"+MAX_LINE_LENGTH);
            file.writeLine("MAX_DIGITS;"+5);
            file.writeLine("MAX_IDENTIFIER_LENGTH;"+10);
            file.closeFile();
        }
        file.openFile(filename);
        String line;
        String[] lineArray;
        while (!file.isEndOfFile()){
            line = file.getNextLine();
            lineArray = line.split(";");
            if (lineArray[0].equals("MAX_LINE_LENGTH")){
                MAX_LINE_LENGTH = Integer.parseInt(lineArray[1]);
            }
            else if (lineArray[0].equals("MAX_DIGITS")){
                MAX_DIGITS = Integer.parseInt(lineArray[1]);
            }
            else if (lineArray[0].equals("MAX_IDENTIFIER_LENGTH")){
                MAX_IDENTIFIER_LENGTH = Integer.parseInt(lineArray[1]);
            }
        }
        file.closeFile();
        System.out.println("Parameters:");
        System.out.println("MAX_LINE_LENGTH: "+MAX_LINE_LENGTH);
        System.out.println("MAX_DIGITS: "+MAX_DIGITS);
        System.out.println("MAX_IDENTIFIER_LENGTH: "+MAX_IDENTIFIER_LENGTH);
        System.out.println();
    }
}
