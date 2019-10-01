package com.uca;

import java.io.*;
import java.nio.charset.Charset;

public class FileManager {
    private BufferedReader fileBuffer = null;
    private boolean endOfFile;
    private char nextChar;

    public void openFile(String fileName){
        try {
            File file = new File(fileName);
            fileBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No se encontro el archivo del programa fuente indicado");
        }
        readNextChar();
    }

    public void closeFile(){
        try {
            fileBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readNextChar(){
        int read = -1;
        try {
            read = fileBuffer.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (read == -1){
            endOfFile = true;
        }
        nextChar = (char)read;
    }

    public boolean hasNextChar(){
        return endOfFile;
    }

    public char getNextChar(){
        char c = nextChar;
        readNextChar();
        return c;
    }
}
