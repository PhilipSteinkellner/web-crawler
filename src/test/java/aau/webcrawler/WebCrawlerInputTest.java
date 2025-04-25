package aau.webcrawler;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebCrawlerInputTest {

    @Test
    void parsesInputArgumentsCorrectly() {
        // Arrange
        String[] args = {"https://example.com", "example.com", "-d", "2"};

        // Act
        WebCrawler crawler = new WebCrawler();
        new CommandLine(crawler).parseArgs(args);

        // Assert
        assertEquals("https://example.com", crawler.getUrl());
        assertEquals(List.of("example.com"), crawler.getDomains());
        assertEquals(2, crawler.getDepth());
    }
}
