package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownFileWriterTest {

    private static final String TEST_FILE_PATH = "test_output.txt";
    private static final Path TEST_PATH = Path.of(TEST_FILE_PATH);
    private MarkdownFileWriter markdownFileWriter;

    @BeforeEach
    void setUp() throws IOException {
        markdownFileWriter = new MarkdownFileWriter(TEST_FILE_PATH);
    }

    @Test
    void testConstructorCreatesFile() throws IOException {
        assertTrue(Files.exists(TEST_PATH));

        Files.delete(TEST_PATH);
    }

    @Test
    void testWriteAppendsContent() throws IOException {
        String content1 = "Hello, World!";
        String content2 = " Goodbye, World!";

        markdownFileWriter.write(content1);

        String fileContent = Files.readString(TEST_PATH);
        assertTrue(fileContent.contains(content1));

        markdownFileWriter.write(content2);

        fileContent = Files.readString(TEST_PATH);
        assertTrue(fileContent.contains(content1));
        assertTrue(fileContent.contains(content2));

        Files.delete(TEST_PATH);
    }

    @Test
    void testWriteCreatesFileIfNotExists() throws IOException {
        Files.deleteIfExists(TEST_PATH);

        assertFalse(Files.exists(TEST_PATH));

        markdownFileWriter.write("Some initial content");

        assertTrue(Files.exists(TEST_PATH));

        Files.delete(TEST_PATH);
    }

    @Test
    void testWriteThrowsIOException() {
        assertThrows(IOException.class, () -> new MarkdownFileWriter("/invalid_path/test_output.txt"));
    }
}
