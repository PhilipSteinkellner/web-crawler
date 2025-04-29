package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void testAnalyze_returnsImmediatelyIfDepthExceedsMax() throws IOException {
        // Arrange: maxDepth is 1, so use depth 2
        analyzer.analyze(URL_TO_ANALYZE, 2);
        // Should not interact with fetcher or recorder
        verifyNoInteractions(websiteFetcher);
        verifyNoInteractions(markdownRecorder);
    }

    @Test
    void testAnalyze_withDepthZero_recordsHeadingsButNotLink() throws IOException {
        Document doc = Jsoup.parse("<h1>Header</h1><a href=\"/next\"></a>");
        when(websiteFetcher.fetch(URL_TO_ANALYZE)).thenReturn(doc);

        analyzer.analyze(URL_TO_ANALYZE, 0);

        verify(markdownRecorder).recordHeadings(any(), any());
        verify(markdownRecorder, never()).recordLink(any(), any());
    }

    @Test
    void testCreateMarkdownIndentation_variousDepths() {
        assertEquals("", analyzer.createMarkdownIndentation(0));
        assertEquals("-->", analyzer.createMarkdownIndentation(1));
        assertEquals("---->", analyzer.createMarkdownIndentation(2));
    }

    @ParameterizedTest
    @CsvSource({
            "https://example.com/page#section, https://example.com/page",
            "https://example.com/page, https://example.com/page",
            "https://example.com/page#, https://example.com/page",
            "'', ''",
            "#section, ''"
    })
    void sanitizeUrl_shouldReturnExpected(String input, String expected) {
        assertEquals(expected, analyzer.sanitizeUrl(input));
    }

}
