package org.example;

import org.example.website.Heading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private List<Heading> createHeadings(String... headingData) {
        List<Heading> headings = new ArrayList<>();
        for (int i = 0; i < headingData.length; i += 2) {
            String tagName = headingData[i];
            String text = headingData[i + 1];
            headings.add(new Heading(tagName, text));
        }
        return headings;
    }

    @Nested
    class RecordInputArgumentsTests {
        @Test
        void writesInputArguments() throws Exception {
            var url = "URL";
            var targetDomains = List.of("Domain");
            var depth = 2;

            markdownRecorder.recordInputArguments(url, targetDomains, depth);

            String expectedContent = "**Input Arguments**" +
                    String.format("%n- URL: %s", url) +
                    String.format("%n- Domains: %s", String.join(", ", targetDomains)) +
                    String.format("%n- Depth: %d%n", depth);

            verify(markdownWriter).write(expectedContent);
        }
    }

    @Nested
    class RecordBrokenLinkTests {
        @Test
        void writesFormattedBrokenLink() throws Exception {
            markdownRecorder.recordBrokenLink(BROKEN_URL, INDENTATION);
            assertRecordLink(BROKEN_URL, INDENTATION, "broken link");
        }

        @Test
        void throwsIOException() throws Exception {
            doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());
            assertThrows(IOException.class, () -> markdownRecorder.recordBrokenLink(BROKEN_URL, INDENTATION));
        }
    }

    @Nested
    class RecordLinkTests {
        @Test
        void writesFormattedLinkWithIndentation() throws Exception {
            markdownRecorder.recordLink(DEFAULT_URL, INDENTATION);
            assertRecordLink(DEFAULT_URL, INDENTATION, "link to");
        }

        @Test
        void writesFormattedLinkWithoutIndentation() throws Exception {
            markdownRecorder.recordLink(DEFAULT_URL, NO_INDENTATION);
            assertRecordLink(DEFAULT_URL, NO_INDENTATION, "link to");
        }

        @Test
        void throwsIOException() throws Exception {
            doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());
            assertThrows(IOException.class, () -> markdownRecorder.recordLink(DEFAULT_URL, INDENTATION));
        }
    }

    @Nested
    class RecordHeadingsTests {
        @Test
        void writesFormattedHeadings() throws IOException {
            List<Heading> headings = createHeadings("h2", "Heading");
            markdownRecorder.recordHeadings(headings, "-->");
            verify(markdownWriter).write(contains("## --> Heading"));
        }

        @Test
        void handlesEmptyIndentation() throws Exception {
            List<Heading> headings = createHeadings("h1", "Heading 1", "h3", "Heading 3");
            markdownRecorder.recordHeadings(headings, NO_INDENTATION);
            verify(markdownWriter).write(String.format("%n#  Heading 1"));
            verify(markdownWriter).write(String.format("%n###  Heading 3"));
        }

        @Test
        void handlesEmptyHeadings() throws Exception {
            markdownRecorder.recordHeadings(new ArrayList<>(), INDENTATION);
            verify(markdownWriter, never()).write(anyString());
        }

        @Test
        void throwsIOException() throws Exception {
            List<Heading> headings = createHeadings("h1", "Heading 1");
            doThrow(new IOException("Write error")).when(markdownWriter).write(anyString());
            assertThrows(IOException.class, () -> markdownRecorder.recordHeadings(headings, INDENTATION));
        }

        @Test
        void writesHeadingWithCorrectPrefix() throws IOException {
            List<Heading> headings = createHeadings("h2", "Section Title");
            String indentation = "-->";
            markdownRecorder.recordHeadings(headings, indentation);
            verify(markdownWriter).write(String.format("%n## --> Section Title"));
        }

        @Test
        void writesSingleHash_whenHeadingIsNotValid() throws IOException {
            List<Heading> headings = createHeadings("p", "Not a Heading");
            String indentation = "  ";
            markdownRecorder.recordHeadings(headings, indentation);
            verify(markdownWriter).write(String.format("%n# %s %s", indentation, "Not a Heading"));
        }
    }
}
