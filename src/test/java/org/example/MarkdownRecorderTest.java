package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarkdownRecorderTest {

    @Mock
    private MarkdownWriter markdownWriter;

    private MarkdownRecorder markdownRecorder;

    @BeforeEach
    void setUp() {
        markdownRecorder = new MarkdownRecorder(markdownWriter);  // inject mock here
    }

    @Test
    void testRecordBrokenLink() throws Exception {
        String url = "http://example.com/broken";
        String indentation = "  ";

        markdownRecorder.recordBrokenLink(url, indentation);  // uses injected mock!

        verify(markdownWriter).write(String.format("%n%n%s broken link %s%n", indentation, url));
    }

    @Test
    void testRecordHeadings_writesFormattedHeadings() throws IOException {
        String htmlContent = "<h2>Heading</h2>";
        Element headingElement = Jsoup.parse(htmlContent).selectFirst("h2");
        Elements headingElements = new Elements(headingElement);
        String indentation = "-->";
        markdownRecorder.recordHeadings(headingElements, indentation);
        verify(markdownWriter, times(1)).write(contains("## --> Heading"));
    }
}
