package org.example;

import java.io.IOException;

public interface MarkdownWriter {
    void write(String content) throws IOException;
}
