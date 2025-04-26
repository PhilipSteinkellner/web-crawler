package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class WebsiteAnalyzerTest {

    private MarkdownFileWriter markdownFileWriter;
    private WebsiteFetcher websiteFetcher;
    private MarkdownRecorder markdownRecorder;
    private WebsiteAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        markdownFileWriter = mock(MarkdownFileWriter.class);
        websiteFetcher = mock(WebsiteFetcher.class);
        markdownRecorder = mock(MarkdownRecorder.class);

        // Create analyzer using real constructor, but inject mocks for collaborators
        analyzer = new WebsiteAnalyzer(
                List.of("example.com"),
                1,
                markdownFileWriter
        ) {
            {
                // Override collaborators
                this.websiteFetcher = WebsiteAnalyzerTest.this.websiteFetcher;
                this.markdownRecorder = WebsiteAnalyzerTest.this.markdownRecorder;
            }
        };
    }

    @Test
    void testAnalyze_withValidDomain_callsMarkdownWrite() throws IOException {
        Document doc = Jsoup.parse("<html><head></head><body><a href='https://example.com/page'>link</a></body></html>");
        when(websiteFetcher.fetch("https://example.com")).thenReturn(doc);

        analyzer.analyze("https://example.com", 1);

        verify(markdownRecorder, atLeastOnce()).recordLink(contains("https://example.com"), any());
    }

    @Test
    void testAnalyze_skipsInvalidUrl_noInteractions() throws IOException {
        analyzer.analyze("ht!tp://invalid-url", 1);

        verifyNoInteractions(markdownRecorder);
        verifyNoInteractions(markdownFileWriter);
    }

    @Test
    void testAnalyze_avoidsDuplicateUrls() throws IOException {
        Document doc = Jsoup.parse("<html><a href='https://example.com'></a></html>");
        when(websiteFetcher.fetch("https://example.com")).thenReturn(doc);

        analyzer.analyze("https://example.com", 1);
        analyzer.analyze("https://example.com", 1);

        verify(websiteFetcher, times(1)).fetch("https://example.com");
    }

    @Test
    void testAnalyze_brokenLinkWritesError() throws IOException {
        when(websiteFetcher.fetch("https://example.com")).thenReturn(null);

        analyzer.analyze("https://example.com", 1);

        verify(markdownRecorder).recordBrokenLink(contains("https://example.com"), any());
    }
}
