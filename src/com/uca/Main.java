package com.uca;

import com.uca.parser.Parser;
import com.uca.pcode.PCodeGenerator;
import com.uca.scanner.Lexicon;
import com.uca.scanner.Scanner;
import com.uca.tools.ErrorLog;
import com.uca.tools.FileManager;
import com.uca.tools.Parameters;

public class Main {

    private static Scanner scanner;

    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Error: No se ha proporcionado el nombre del programa fuente");
            return;
        }
        String fileName = args[0];
        compile(fileName);
    }

    private static void compile(String fileName){
        System.out.println("=== Compilador de SpegMoe ===");
        FileManager.createDirectory("output");
        Parameters.loadFromFile("parameters.txt");
        ErrorLog.init();
        Lexicon.init();
        scanner = new Scanner();
        PCodeGenerator pCodeGenerator = new PCodeGenerator(fileName);
        Parser parser = new Parser(scanner, pCodeGenerator);

        scanner.scan(fileName);
        parser.parse();

        scanner.close();
        ErrorLog.close();
    }

    public static void close(){
        scanner.close();
        ErrorLog.close();
        System.exit(0);
    }
}
