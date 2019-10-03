package com.uca;

import java.io.*;
import java.nio.charset.Charset;

public class FileManager {
    private BufferedReader fileBuffer = null;
    private boolean endOfFile;
    private String line;
    private int lineOffset;

    public void openFile(String fileName){
        try {
            File file = new File(fileName);
            fileBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No se encontro el archivo del programa fuente indicado");
        }
        readNextLine();
    }

    public void closeFile(){
        try {
            fileBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readNextLine(){
        String line = null;
        try {
            line = fileBuffer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line == null){
            endOfFile = true;
            this.line = "";
            return;
        }
        if (line.length() > Parameters.MAX_LINE_LENGTH){
            System.out.println("La linea se pasa del maximo de caracteres");
            line.substring(0,Parameters.MAX_LINE_LENGTH);
        }
        this.line = line;
        lineOffset = 0;
    }

    private char readNextChar(){
        char character;
        if (!endOfFile){
            character = line.charAt(lineOffset);
            lineOffset++;
            if (lineOffset > line.length()-1){
                readNextLine();
            }
        }else{
            character = ' ';
        }
        return character;
    }

    public char getNextChar(){
        return readNextChar();
    }

    public boolean isEndOfFile(){
        return endOfFile;
    }
}
