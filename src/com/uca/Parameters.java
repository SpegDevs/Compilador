package com.uca;

public class Parameters {

    private static final String MAX_LINE_LENGTH_TAG = "MAX_LINE_LENGTH";
    private static final String MAX_DIGITS_TAG = "MAX_DIGITS";
    private static final String MAX_IDENTIFIER_LENGTH_TAG = "MAX_IDENTIFIER_LENGTH";
    private static final String MAX_SYMBOL_TABLE_ITEMS_TAG = "MAX_SYMBOL_TABLE_ITEMS";
    private static final String MAX_BLOCK_NESTING_TAG = "MAX_BLOCK_NESTING";
    private static final String MAX_INTEGER_SIZE_TAG = "MAX_INTEGER_SIZE";

    public static int MAX_LINE_LENGTH = 1000;
    public static int MAX_DIGITS = 5;
    public static int MAX_IDENTIFIER_LENGTH = 10;
    public static int MAX_SYMBOL_TABLE_ITEMS = 100;
    public static int MAX_BLOCK_NESTING = 3;
    public static int MAX_INTEGER_SIZE = 32767;

    public static void loadFromFile(String fileName){
        FileManager file = new FileManager(fileName);
        if (!file.fileExists()) {
            file.createFile();
            file.writeLine(MAX_LINE_LENGTH_TAG+";"+MAX_LINE_LENGTH);
            file.writeLine(MAX_DIGITS_TAG+";"+MAX_DIGITS);
            file.writeLine(MAX_IDENTIFIER_LENGTH_TAG+";"+MAX_IDENTIFIER_LENGTH);
            file.writeLine(MAX_SYMBOL_TABLE_ITEMS_TAG+";"+MAX_SYMBOL_TABLE_ITEMS);
            file.writeLine(MAX_BLOCK_NESTING_TAG+";"+MAX_BLOCK_NESTING);
            file.writeLine(MAX_INTEGER_SIZE_TAG+";"+MAX_INTEGER_SIZE);
            file.closeFile();
        }
        file.openFile();
        String line;
        String[] lineArray;
        while (!file.isEndOfFile()){
            line = file.getNextLine();
            lineArray = line.split(";");
            switch (lineArray[0]){
                case MAX_LINE_LENGTH_TAG:
                    MAX_LINE_LENGTH = Integer.parseInt(lineArray[1]);
                    break;
                case MAX_DIGITS_TAG:
                    MAX_DIGITS = Integer.parseInt(lineArray[1]);
                    break;
                case MAX_IDENTIFIER_LENGTH_TAG:
                    MAX_IDENTIFIER_LENGTH = Integer.parseInt(lineArray[1]);
                    break;
                case MAX_SYMBOL_TABLE_ITEMS_TAG:
                    MAX_SYMBOL_TABLE_ITEMS = Integer.parseInt(lineArray[1]);
                    break;
                case MAX_BLOCK_NESTING_TAG:
                    MAX_BLOCK_NESTING = Integer.parseInt(lineArray[1]);
                    break;
                case MAX_INTEGER_SIZE_TAG:
                    MAX_INTEGER_SIZE = Integer.parseInt(lineArray[1]);
                    break;
            }
        }
        file.closeFile();
        System.out.println("Parameters:");
        System.out.println(MAX_LINE_LENGTH_TAG+": "+MAX_LINE_LENGTH);
        System.out.println(MAX_DIGITS_TAG+": "+MAX_DIGITS);
        System.out.println(MAX_IDENTIFIER_LENGTH_TAG+": "+MAX_IDENTIFIER_LENGTH);
        System.out.println(MAX_SYMBOL_TABLE_ITEMS_TAG+": "+MAX_SYMBOL_TABLE_ITEMS);
        System.out.println(MAX_BLOCK_NESTING_TAG+": "+MAX_BLOCK_NESTING);
        System.out.println(MAX_INTEGER_SIZE_TAG+": "+MAX_INTEGER_SIZE);
        System.out.println();
    }
}
