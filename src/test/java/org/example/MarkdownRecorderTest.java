package org.example;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkdownRecorderTest {

    private static final String DEFAULT_URL = "https://example.com";
    private static final String BROKEN_URL = "https://example.com/broken";
    private static final String INDENTATION = "  ";
    private static final String NO_INDENTATION = "";

    @Mock
    private MarkdownWriter markdownWriter;

    private MarkdownRecorder markdownRecorder;

    @BeforeEach
    void setUp() {
        markdownRecorder = new MarkdownRecorder(markdownWriter);
    }

    private void assertRecordLink(String url, String indentation, String linkType) throws IOException {
        String expectedOutput = String.format("%n%n%s %s %s%n", indentation, linkType, url);
        verify(markdownWriter).write(expectedOutput);
    }

    private Elements createHeadingElements(String html) {
        return new Elements(Jsoup.parse(html).select("h1, h2, h3"));
    }

    @Test
    void recordBrokenLink_writesFormattedBrokenLink() throws Exception {
        markdownRecorder.recordBrokenLink(BROKEN_URL, INDENTATION);
        assertRecordLink(BROKEN_URL, INDENTATION, "broken link");
    }

    @Test
    void recordBrokenLink_throwsIOException() throws Exception {
        doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());
        assertThrows(IOException.class, () -> markdownRecorder.recordBrokenLink(BROKEN_URL, INDENTATION));
    }

    @Test
    void recordLink_writesFormattedLinkWithIndentation() throws Exception {
        markdownRecorder.recordLink(DEFAULT_URL, INDENTATION);
        assertRecordLink(DEFAULT_URL, INDENTATION, "link to");
    }

    @Test
    void recordLink_writesFormattedLinkWithoutIndentation() throws Exception {
        markdownRecorder.recordLink(DEFAULT_URL, NO_INDENTATION);
        assertRecordLink(DEFAULT_URL, NO_INDENTATION, "link to");
    }

    @Test
    void recordLink_throwsIOException() throws Exception {
        doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());
        assertThrows(IOException.class, () -> markdownRecorder.recordLink(DEFAULT_URL, INDENTATION));
    }

    @Test
    void recordHeadings_writesFormattedHeadings() throws IOException {
        Elements headings = createHeadingElements("<h2>Heading</h2>");
        markdownRecorder.recordHeadings(headings, "-->");
        verify(markdownWriter).write(contains("## --> Heading"));
    }

    @Test
    void recordHeadings_handlesEmptyIndentation() throws Exception {
        Elements headings = createHeadingElements("<h1>Heading 1</h1><h3>Heading 3</h3>");
        markdownRecorder.recordHeadings(headings, NO_INDENTATION);

        verify(markdownWriter).write(String.format("%n#  Heading 1"));
        verify(markdownWriter).write(String.format("%n###  Heading 3"));
    }

    @Test
    void recordHeadings_handlesEmptyHeadings() throws Exception {
        markdownRecorder.recordHeadings(new Elements(), INDENTATION);
        verify(markdownWriter, never()).write(anyString());
    }

    @Test
    void recordHeadings_throwsIOException() throws Exception {
        Elements headings = createHeadingElements("<h1>Heading 1</h1>");
        doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());

        assertThrows(IOException.class, () -> markdownRecorder.recordHeadings(headings, INDENTATION));
    }
}
