import org.example.FileWriter;
import org.example.WebsiteAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class WebsiteAnalyzerTest {

    private FileWriter fileWriter;
    private WebsiteAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        fileWriter = mock(FileWriter.class);
        analyzer = new WebsiteAnalyzer(List.of("example.com"), 1, fileWriter);
    }

    @Test
    void testCheckHostValidity_withValidDomain() throws Exception {
        // public method indirectly calls checkHostValidity
        Document doc = Jsoup.parse("<html><head></head><body><a href='https://example.com/page'>link</a></body></html>");
        WebsiteAnalyzer spyAnalyzer = Mockito.spy(analyzer);
        doReturn(doc).when(spyAnalyzer).fetchAndParseWebsite("https://example.com");

        spyAnalyzer.analyze("https://example.com", 1);

        verify(fileWriter, atLeastOnce()).write(contains("link to"));
    }

    @Test
    void testAnalyze_skipsInvalidUrl() throws Exception {
        analyzer.analyze("ht!tp://invalid-url", 1);
        // Should not call fileWriter
        verifyNoInteractions(fileWriter);
    }

    @Test
    void testAnalyze_avoidsDuplicateUrls() throws Exception {
        Document doc = Jsoup.parse("<html><a href='https://example.com'></a></html>");
        WebsiteAnalyzer spyAnalyzer = Mockito.spy(analyzer);
        doReturn(doc).when(spyAnalyzer).fetchAndParseWebsite("https://example.com");

        spyAnalyzer.analyze("https://example.com", 1);
        spyAnalyzer.analyze("https://example.com", 1);

        verify(fileWriter, times(1)).write(anyString());
    }

    @Test
    void testAnalyze_brokenLinkWritesError() throws Exception {
        WebsiteAnalyzer spyAnalyzer = Mockito.spy(analyzer);
        doReturn(null).when(spyAnalyzer).fetchAndParseWebsite("https://example.com");

        spyAnalyzer.analyze("https://example.com", 1);

        verify(fileWriter).write(contains("broken link"));
    }

    @Test
    void testRecordHeadings_writesFormattedHeadings() throws Exception {
        Element h2 = Jsoup.parse("<h2>Heading</h2>").selectFirst("h2");
        Elements headings = new Elements(h2);

        analyzer.recordHeadings(headings, "-->");

        verify(fileWriter).write(contains("## --> Heading"));
    }
}