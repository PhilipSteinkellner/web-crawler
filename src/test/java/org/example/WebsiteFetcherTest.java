package org.example;

import org.example.website.Heading;
import org.example.website.Link;
import org.example.website.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebsiteFetcherTest {

    public static final String TEST_URL = "https://example.com";
    private WebsiteFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new WebsiteFetcher();
    }

    @Test
    void fetchPage_returnsBrokenPage_whenUrlIsNull() {
        Page result = fetcher.fetchPage(null, 0);
        assertNotNull(result);
        assertTrue(result.broken());
        assertEquals(0, result.depth());
    }

    @Test
    void fetchPage_returnsBrokenPage_whenUrlIsEmpty() {
        Page result = fetcher.fetchPage("", 0);
        assertNotNull(result);
        assertTrue(result.broken());
        assertEquals(0, result.depth());
    }

    @Test
    void fetchPage_returnsValidPage_whenFetchSuccessful() throws IOException {
        Document mockDocument = mock(Document.class);
        when(mockDocument.select("a[href]")).thenReturn(new org.jsoup.select.Elements());
        when(mockDocument.select("h1, h2, h3, h4, h5, h6")).thenReturn(new org.jsoup.select.Elements());

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);

            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenReturn(mockDocument);

            Page result = fetcher.fetchPage(TEST_URL, 1);

            assertNotNull(result);
            assertFalse(result.broken());
            assertEquals(TEST_URL, result.url());
            assertEquals(1, result.depth());
            jsoupMock.verify(() -> Jsoup.connect(TEST_URL));
            verify(connectionMock).get();
        }
    }

    @Test
    void fetchPage_returnsBrokenPage_whenIOExceptionOccurs() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);

            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenThrow(new IOException("Connection failed"));

            Page result = fetcher.fetchPage(TEST_URL, 1);

            assertNotNull(result);
            assertTrue(result.broken());
            assertEquals(TEST_URL, result.url());
            assertEquals(1, result.depth());
            jsoupMock.verify(() -> Jsoup.connect(TEST_URL));
            verify(connectionMock).get();
        }
    }

    @Test
    void fetchPage_extractsAllLinksAndHeadings() throws IOException {
        String htmlContent = "<html>"
                + "<body>"
                + "<a href='https://example.com/page1'>Link1</a>"
                + "<a href='/page2'>Link2</a>"
                + "<h1>Main Heading</h1>"
                + "<h2>Sub Heading</h2>"
                + "</body></html>";

        Document realDocument = Jsoup.parse(htmlContent, TEST_URL);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenReturn(realDocument);

            Page result = fetcher.fetchPage(TEST_URL, 1);

            List<Link> links = result.links();
            assertEquals(2, links.size());
            assertEquals("https://example.com/page1", links.get(0).href());
            assertEquals("https://example.com/page2", links.get(1).href());

            List<Heading> headings = result.headings();
            assertEquals(2, headings.size());
            assertEquals("h1", headings.get(0).tagName());
            assertEquals("Main Heading", headings.get(0).text());
            assertEquals("h2", headings.get(1).tagName());
            assertEquals("Sub Heading", headings.get(1).text());
        }
    }

    @Test
    void fetchPage_handlesRelativeLinksCorrectly() throws IOException {
        String htmlContent = "<a href='/about'>About</a>";
        Document realDocument = Jsoup.parse(htmlContent, TEST_URL);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenReturn(realDocument);

            Page result = fetcher.fetchPage(TEST_URL, 1);

            assertEquals("https://example.com/about", result.links().get(0).href());
        }
    }
}
