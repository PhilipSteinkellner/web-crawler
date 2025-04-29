package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownFileWriterTest {

    private static final String TEST_FILE_PATH = "test_output.txt";
    private MarkdownFileWriter markdownFileWriter;

    @BeforeEach
    void setUp() throws IOException {
        markdownFileWriter = new MarkdownFileWriter(TEST_FILE_PATH);
    }

    @Test
    void testConstructorCreatesFile() throws IOException {
        Path filePath = Path.of(TEST_FILE_PATH);
        assertTrue(Files.exists(filePath), "File should be created on construction");

        Files.delete(filePath);
    }

    @Test
    void testWriteAppendsContent() throws IOException {
        String content1 = "Hello, World!";
        String content2 = " Goodbye, World!";

        markdownFileWriter.write(content1);

        String fileContent = Files.readString(Path.of(TEST_FILE_PATH));
        assertTrue(fileContent.contains(content1), "File should contain the first written content");

        markdownFileWriter.write(content2);

        fileContent = Files.readString(Path.of(TEST_FILE_PATH));
        assertTrue(fileContent.contains(content1), "File should still contain the first content");
        assertTrue(fileContent.contains(content2), "File should contain the second content");

        Files.delete(Path.of(TEST_FILE_PATH));
    }

    @Test
    void testWriteCreatesFileIfNotExists() throws IOException {
        Files.deleteIfExists(Path.of(TEST_FILE_PATH));

        assertFalse(Files.exists(Path.of(TEST_FILE_PATH)), "File should not exist before writing");

        markdownFileWriter.write("Some initial content");

        assertTrue(Files.exists(Path.of(TEST_FILE_PATH)), "File should be created after writing content");

        Files.delete(Path.of(TEST_FILE_PATH));
    }

    @Test
    void testWriteThrowsIOException() {
        assertThrows(IOException.class, () -> new MarkdownFileWriter("/invalid_path/test_output.txt"), "IOException should be thrown");
    }
}
