package com.uca.tools;

import com.uca.Main;

import java.io.*;
import java.nio.charset.Charset;

public class FileManager {
    private String fileName;
    private BufferedReader fileBuffer = null;
    private BufferedWriter fileWriter = null;
    private boolean endOfFile;
    private String line="";
    private int lineOffset=0;
    private int lineCount=0;

    public static void createDirectory(String dir){
        File file = new File(dir);
        if (!file.exists()){
            file.mkdir();
        }
    }

    public FileManager(String fileName){
        this.fileName = fileName;
    }

    public boolean fileExists(){
        File file = new File(fileName);
        return file.exists();
    }

    public void createFile(){
        File file = new File(fileName);
        try {
            file.createNewFile();
            openFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openFile(){
        try {
            File file = new File(fileName);
            fileBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            ErrorLog.logError("Error: No se encontro el archivo \""+fileName+"\"");
            Main.close();
            System.exit(0);
        }
        readNextLine();
    }

    public void clearFile(){
        try {
            PrintWriter writer = new PrintWriter(fileName);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
                fileBuffer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readNextLine(){
        if (fileBuffer == null){
            endOfFile = true;
            return;
        }
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
            ErrorLog.logError("Error: La linea "+(lineCount+1)+" sobrepasa del maximo de caracteres ("+Parameters.MAX_LINE_LENGTH+").");
            line = line.substring(0,Parameters.MAX_LINE_LENGTH);
        }
        this.line = line;
        lineOffset = 0;
        lineCount++;
    }

    private char readNextChar(){
        char character;
        if (!endOfFile){
            if (lineOffset > line.length()-1){
                readNextLine();
                return '\n';
            }
            character = line.charAt(lineOffset);
            lineOffset++;
        }else{
            character = ' ';
        }
        return character;
    }

    public char getNextChar(){
        return readNextChar();
    }

    public String getNextLine(){
        String line = this.line;
        readNextLine();
        return line;
    }

    public void writeLine(String line){
        try {
            fileWriter.write(line);
            fileWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEndOfFile(){
        return endOfFile;
    }

    public int getLineCount(){
        return lineCount;
    }

    public int getCharCount(){
        return lineOffset;
    }
}
