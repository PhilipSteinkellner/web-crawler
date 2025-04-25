package aau.webcrawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MarkdownFileWriter {

    private final String filePath;

    // WARNING: This constructor truncates the file if it already exists.
    public MarkdownFileWriter(String filePath) throws IOException {
        this.filePath = filePath;
        Files.write(Paths.get(filePath), "".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void write(String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
