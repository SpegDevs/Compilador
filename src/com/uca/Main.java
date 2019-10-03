package com.uca;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("No se ha proporcionado el nombre del programa fuente");
            return;
        }
        String fileName = args[0];
        compile(fileName);
    }

    private static void compile(String fileName){
        Scanner scanner = new Scanner();
        scanner.scan(fileName);
    }
}
