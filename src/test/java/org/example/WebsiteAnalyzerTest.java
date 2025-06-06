package org.example;

import org.example.website.Page;
import org.example.website.Link;
import org.example.website.Heading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebsiteAnalyzerTest {

    public static final String URL_TO_ANALYZE = "https://example.com";
    private WebsiteFetcher websiteFetcher;
    private MarkdownRecorder markdownRecorder;
    private WebsiteAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MarkdownFileWriter markdownFileWriter = mock(MarkdownFileWriter.class);
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
    void testAnalyze_withValidDomain_returnsLinks() throws IOException {
        Page mockPage = new Page(URL_TO_ANALYZE, 1, false,
                Collections.emptyList(),
                List.of(new Link("https://example.com/page1")));
        when(websiteFetcher.fetchPage(URL_TO_ANALYZE, 1)).thenReturn(mockPage);

        List<Link> result = analyzer.analyze(URL_TO_ANALYZE, 1);

        assertEquals(1, result.size());
        assertEquals("https://example.com/page1", result.get(0).href());
        verify(websiteFetcher).fetchPage(URL_TO_ANALYZE, 1);
    }

    @Test
    void testAnalyze_skipsInvalidUrl_returnsEmptyList() throws IOException {
        List<Link> result = analyzer.analyze("ht!tp://invalid-url", 1);

        assertTrue(result.isEmpty());
        verifyNoInteractions(websiteFetcher);
    }

    @Test
    void testAnalyze_avoidsDuplicateUrls() throws IOException {
        Page mockPage = new Page(URL_TO_ANALYZE, 1, false,
                Collections.emptyList(),
                List.of(new Link("https://example.com/page1")));
        when(websiteFetcher.fetchPage(URL_TO_ANALYZE, 1)).thenReturn(mockPage);

        analyzer.analyze(URL_TO_ANALYZE, 1);
        List<Link> result = analyzer.analyze(URL_TO_ANALYZE, 1);

        assertTrue(result.isEmpty());
        verify(websiteFetcher, times(1)).fetchPage(URL_TO_ANALYZE, 1);
    }

    @Test
    void testAnalyze_brokenLinkReturnsEmptyList() throws IOException {
        Page brokenPage = new Page(URL_TO_ANALYZE, 1, true,
                Collections.emptyList(), Collections.emptyList());
        when(websiteFetcher.fetchPage(URL_TO_ANALYZE, 1)).thenReturn(brokenPage);

        List<Link> result = analyzer.analyze(URL_TO_ANALYZE, 1);

        assertTrue(result.isEmpty());
        verify(websiteFetcher).fetchPage(URL_TO_ANALYZE, 1);
    }

    @Test
    void testAnalyze_returnsImmediatelyIfDepthExceedsMax() throws IOException {
        List<Link> result = analyzer.analyze(URL_TO_ANALYZE, 2);

        assertTrue(result.isEmpty());
        verifyNoInteractions(websiteFetcher);
    }

    @Test
    void testAnalyze_withDepthZero_stillReturnsLinks() throws IOException {
        Page mockPage = new Page(URL_TO_ANALYZE, 0, false,
                List.of(new Heading("h1", "Header")),
                List.of(new Link("https://example.com/next")));
        when(websiteFetcher.fetchPage(URL_TO_ANALYZE, 0)).thenReturn(mockPage);

        List<Link> result = analyzer.analyze(URL_TO_ANALYZE, 0);

        assertEquals(1, result.size());
        assertEquals("https://example.com/next", result.get(0).href());
    }

    @Test
    void testWriteReport_callsRecorderForEachPage() throws IOException {
        Page page1 = new Page(URL_TO_ANALYZE, 0, false,
                List.of(new Heading("h1", "Header")), Collections.emptyList());
        Page page2 = new Page("https://example.com/broken", 1, true,
                Collections.emptyList(), Collections.emptyList());

        try {
            java.lang.reflect.Field pagesField = WebsiteAnalyzer.class.getDeclaredField("pages");
            pagesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Page> pages = (List<Page>) pagesField.get(analyzer);
            pages.add(page1);
            pages.add(page2);

            analyzer.writeReport();

            verify(markdownRecorder).recordHeadings(any(), eq(""));
            verify(markdownRecorder).recordBrokenLink("https://example.com/broken", "-->");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed: " + e.getMessage());
        }
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
        try {
            java.lang.reflect.Method method = WebsiteAnalyzer.class.getDeclaredMethod("sanitizeUrl", String.class);
            method.setAccessible(true);
            assertEquals(expected, method.invoke(null, input));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testCreateMarkdownIndentation_variousDepths() {
        try {
            java.lang.reflect.Method method = WebsiteAnalyzer.class.getDeclaredMethod("createMarkdownIndentation", int.class);
            method.setAccessible(true);
            assertEquals("", method.invoke(null, 0));
            assertEquals("-->", method.invoke(null, 1));
            assertEquals("---->", method.invoke(null, 2));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void recordInputArguments_DelegatesToMarkdownRecorder_WithCorrectParameters() throws IOException {
        String url = "https://example.com";
        List<String> targetDomains = List.of("example.com");
        int maxDepth = 2;

        analyzer.recordInputArguments(url, targetDomains, maxDepth);

        verify(markdownRecorder).recordInputArguments(url, targetDomains, maxDepth);
    }

    @Test
    void startAnalysis_HandlesConcurrentCrawling_WithoutDuplicates() throws IOException {
        String url = URL_TO_ANALYZE;
        Page mockPage = new Page(url, 0, false,
                Collections.emptyList(),
                List.of(new Link(url + "/page1"), new Link(url + "/page2")));

        when(websiteFetcher.fetchPage(any(), anyInt())).thenReturn(mockPage);

        analyzer.startAnalysis(url);

        verify(websiteFetcher, times(3)).fetchPage(any(), anyInt());
        verify(markdownRecorder, atLeastOnce()).recordLink(any(), any());
    }
}
