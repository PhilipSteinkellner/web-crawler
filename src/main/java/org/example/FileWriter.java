package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriter {

    private final String fileName;

    public FileWriter(String fileName) {
        this.fileName = fileName;
    }

    public void write(String content) throws IOException {
        Files.write(Paths.get(this.fileName), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
