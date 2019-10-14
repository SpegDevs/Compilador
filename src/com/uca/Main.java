package com.uca;

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
        Parser parser = new Parser(scanner);

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
