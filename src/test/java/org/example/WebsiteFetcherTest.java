package org.example;

import org.example.website.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

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
}
