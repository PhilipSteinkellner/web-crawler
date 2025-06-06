package org.example;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebCrawlerTest {

    private static final String TEST_URL = "https://example.com";
    private static final String EXAMPLE_COM = "example.com";
    private static final int TEST_DEPTH = 1;

    private WebCrawler createCrawler(List<String> domains) {
        WebCrawler crawler = new WebCrawler();
        crawler.setUrl(WebCrawlerTest.TEST_URL);
        crawler.setDomains(domains);
        crawler.setDepth(WebCrawlerTest.TEST_DEPTH);
        return crawler;
    }

    @Test
    void parsesInputArgumentsCorrectly() {
        String[] args = {TEST_URL, EXAMPLE_COM, "-d", String.valueOf(TEST_DEPTH)};

        WebCrawler crawler = new WebCrawler();
        new CommandLine(crawler).parseArgs(args);

        assertEquals(TEST_URL, crawler.getUrl());
        assertEquals(List.of(EXAMPLE_COM), crawler.getDomains());
        assertEquals(TEST_DEPTH, crawler.getDepth());
    }

    @Test
    void call_writesInputArgumentsAndTriggersAnalysis() throws Exception {
        WebCrawler crawler = createCrawler(List.of(EXAMPLE_COM));

        try (MockedConstruction<MarkdownFileWriter> markdownWriterMock = mockConstruction(MarkdownFileWriter.class);
             MockedConstruction<WebsiteAnalyzer> analyzerMock = mockConstruction(WebsiteAnalyzer.class,
                     (mockAnalyzer, context) -> {
                         doNothing().when(mockAnalyzer).recordInputArguments(anyString(), anyList(), anyInt());
                         doNothing().when(mockAnalyzer).startAnalysis(anyString());
                     })
        ) {
            Integer result = crawler.call();

            // Check that at least one instance was created
            assertFalse(analyzerMock.constructed().isEmpty());
            WebsiteAnalyzer analyzer = analyzerMock.constructed().get(0);

            verify(analyzer).recordInputArguments(TEST_URL, List.of(EXAMPLE_COM), TEST_DEPTH);
            verify(analyzer).startAnalysis(TEST_URL);
            assertEquals(0, result);
        }
    }

    @Test
    void call_propagatesIOExceptionFromAnalysis() {
        WebCrawler crawler = createCrawler(List.of(EXAMPLE_COM));

        try (MockedConstruction<MarkdownFileWriter> markdownWriterMock = mockConstruction(MarkdownFileWriter.class);
             MockedConstruction<WebsiteAnalyzer> analyzerMock = mockConstruction(WebsiteAnalyzer.class,
                     (mockAnalyzer, context) -> {
                         doNothing().when(mockAnalyzer).recordInputArguments(anyString(), anyList(), anyInt());
                         doThrow(new IOException("Analysis failed")).when(mockAnalyzer).startAnalysis(anyString());
                     })
        ) {
            assertThrows(IOException.class, crawler::call);
        }
    }
}
