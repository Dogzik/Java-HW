package ru.ifmo.rain.dovzhik.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final static int START = 0x811c9dc5;
    private final static int BIG_STEP = 0x01000193;
    private final static int SMALL_STEP = 0xff;
    private final static int BUFF_SIZE = 8192;
    private byte[] buff = new byte[BUFF_SIZE];
    private final BufferedWriter writer;

    FileVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    private FileVisitResult writeData(int hash, Path file) throws IOException {
        writer.write(String.format("%08x", hash) + " " + file);
        writer.newLine();
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        int hash = START;
        try (InputStream reader = Files.newInputStream(file)) {
            int cnt;
            while ((cnt = reader.read(buff)) != -1) {
                for (int i = 0; i < cnt; i++) {
                    hash = (hash * BIG_STEP) ^ (buff[i] & SMALL_STEP);
                }
            }
        } catch (IOException e) {
            hash = 0;
        }
        return writeData(hash, file);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return writeData(0, file);
    }
}
