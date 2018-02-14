package ru.ifmo.rain.dovzhik.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class RecursiveWalk {
    private final Path inputPath;
    private final Path outputPath;

    RecursiveWalk(final String input, final String output) throws WalkerException {
        try {
            inputPath = Paths.get(input);
        } catch (InvalidPathException e) {
            throw new WalkerException("Incorrect path to input file: " + e.getMessage());
        }
        try {
            outputPath = Paths.get(output);
        } catch (InvalidPathException e) {
            throw new WalkerException("Incorrect path to output file: " + e.getMessage());
        }
        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                throw new WalkerException("Unable to create folder(s) for output file:" + e.getMessage());
            }
        }
    }

    public void walk() throws WalkerException {
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String path;
                FileVisitor visitor = new FileVisitor(writer);
                try {
                    while ((path = reader.readLine()) != null) {
                        try {
                            Files.walkFileTree(Paths.get(path), visitor);
                        } catch (InvalidPathException e) {
                            writer.write("00000000 " + path);
                        }
                    }
                } catch (IOException e) {
                    throw new WalkerException(e.getMessage());
                }
            } catch (IOException e) {
                throw new WalkerException("Unable to process output file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkerException("Unable to process input file: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new WalkerException("Expected arguments: <input file> <output file>");
            }
            RecursiveWalk walker = new RecursiveWalk(args[0], args[1]);
            walker.walk();
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }
}
