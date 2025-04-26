package org.example;

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

    private WebsiteFetcher websiteFetcher;

    @BeforeEach
    void setUp() {
        websiteFetcher = new WebsiteFetcher();
    }

    @Test
    void fetch_validUrl_returnsDocument() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {

            Document mockDocument = mock(Document.class);

            Connection mockConnection = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            Document result = websiteFetcher.fetch("http://example.com");

            assertNotNull(result);
            assertEquals(mockDocument, result);

            jsoupMock.verify(() -> Jsoup.connect("http://example.com"), times(1));
        }
    }

    @Test
    void fetch_invalidUrl_returnsNull() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {

            Connection mockConnection = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect("http://invalid-url")).thenReturn(mockConnection);
            when(mockConnection.get()).thenThrow(new IOException());

            Document result = websiteFetcher.fetch("http://invalid-url");

            assertNull(result);

            jsoupMock.verify(() -> Jsoup.connect("http://invalid-url"), times(1));
        }
    }

    @Test
    void fetch_nullUrl_returnsNull() {
        Document result = websiteFetcher.fetch(null);
        assertNull(result);
    }
}