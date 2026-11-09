package com.chaukz.stacktrace.cli;


import java.nio.file.Files;
import java.nio.file.Path;

public class StackTraceCli {
    public void run(String[] args){

        if (args.length == 0) {
            System.out.println("Usage: stacktrace <command> [options]");
            return;
        }
        switch (args[0]) {
            case "index":
                if (args.length < 2) {
                    System.out.println("Usage: stacktrace index <directory>");
                    return;
                }

                Path directory = Path.of(args[1]);
                if (!Files.exists(directory)) {
                System.out.println("Indexing directory: " + directory);
                return;
                 }
                if (!Files.isDirectory(directory)) {
                    System.out.println("Path is not a directory: "+ directory);
                    return;
                }
                System.out.println("Indexing directory: " + directory);
                break;

            case "search":
                System.out.println("Search command selected");
                break;

            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}
