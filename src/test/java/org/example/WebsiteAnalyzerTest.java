package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class WebsiteAnalyzerTest {

    public static final String URL_TO_ANALYZE = "https://example.com";
    private MarkdownFileWriter markdownFileWriter;
    private WebsiteFetcher websiteFetcher;
    private MarkdownRecorder markdownRecorder;
    private WebsiteAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        markdownFileWriter = mock(MarkdownFileWriter.class);
        websiteFetcher = mock(WebsiteFetcher.class);
        markdownRecorder = mock(MarkdownRecorder.class);

        analyzer = new WebsiteAnalyzer(
                List.of("example.com"),
                1,
                markdownFileWriter
        ) {
            {
                this.websiteFetcher = WebsiteAnalyzerTest.this.websiteFetcher;
                this.markdownRecorder = WebsiteAnalyzerTest.this.markdownRecorder;
            }
        };
    }

    @Test
    void testAnalyze_withValidDomain_callsMarkdownWrite() throws IOException {
        Document doc = Jsoup.parse("<html><head></head><body><a href='" + URL_TO_ANALYZE + "'>link</a></body></html>");
        when(websiteFetcher.fetch(URL_TO_ANALYZE)).thenReturn(doc);

        analyzer.analyze(URL_TO_ANALYZE, 1);

        verify(markdownRecorder, atLeastOnce()).recordLink(contains(URL_TO_ANALYZE), any());
    }

    @Test
    void testAnalyze_skipsInvalidUrl_noInteractions() throws IOException {
        analyzer.analyze("ht!tp://invalid-url", 1);

        verifyNoInteractions(markdownRecorder);
        verifyNoInteractions(markdownFileWriter);
    }

    @Test
    void testAnalyze_avoidsDuplicateUrls() throws IOException {
        Document doc = Jsoup.parse("<html><a href='" + URL_TO_ANALYZE + "'></a></html>");
        when(websiteFetcher.fetch(URL_TO_ANALYZE)).thenReturn(doc);

        analyzer.analyze(URL_TO_ANALYZE, 1);
        analyzer.analyze(URL_TO_ANALYZE, 1);

        verify(websiteFetcher, times(1)).fetch(URL_TO_ANALYZE);
    }

    @Test
    void testAnalyze_brokenLinkWritesError() throws IOException {
        when(websiteFetcher.fetch(URL_TO_ANALYZE)).thenReturn(null);

        analyzer.analyze(URL_TO_ANALYZE, 1);

        verify(markdownRecorder).recordBrokenLink(contains(URL_TO_ANALYZE), any());
    }

    @Test
    void shouldRemoveFragmentFromUrl() {
        String urlWithFragment = URL_TO_ANALYZE + "/page#section1";
        String sanitized = analyzer.sanitizeUrl(urlWithFragment);
        assertEquals(URL_TO_ANALYZE + "/page", sanitized);
    }

    @Test
    void shouldReturnUrlUnchangedIfNoFragment() {
        String urlWithoutFragment = URL_TO_ANALYZE + "/page";
        String sanitized = analyzer.sanitizeUrl(urlWithoutFragment);
        assertEquals(URL_TO_ANALYZE + "/page", sanitized);
    }

    @Test
    void shouldHandleUrlEndingWithHash() {
        String urlEndingWithHash = URL_TO_ANALYZE + "/page#";
        String sanitized = analyzer.sanitizeUrl(urlEndingWithHash);
        assertEquals(URL_TO_ANALYZE + "/page", sanitized);
    }

    @Test
    void shouldHandleEmptyString() {
        String emptyUrl = "";
        String sanitized = analyzer.sanitizeUrl(emptyUrl);
        assertEquals("", sanitized);
    }

    @Test
    void shouldHandleOnlyFragment() {
        String onlyFragment = "#section";
        String sanitized = analyzer.sanitizeUrl(onlyFragment);
        assertEquals("", sanitized);
    }
}
