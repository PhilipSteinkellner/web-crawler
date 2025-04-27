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

    public static final String FETCH_URL = "https://example.com";
    public static final String INVALID_URL = "http://invalid-url";
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
            jsoupMock.when(() -> Jsoup.connect(FETCH_URL)).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            Document result = websiteFetcher.fetch(FETCH_URL);

            assertNotNull(result);
            assertEquals(mockDocument, result);

            jsoupMock.verify(() -> Jsoup.connect(FETCH_URL), times(1));
        }
    }

    @Test
    void fetch_invalidUrl_returnsNull() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {

            Connection mockConnection = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(INVALID_URL)).thenReturn(mockConnection);
            when(mockConnection.get()).thenThrow(new IOException());

            Document result = websiteFetcher.fetch(INVALID_URL);

            assertNull(result);

            jsoupMock.verify(() -> Jsoup.connect(INVALID_URL), times(1));
        }
    }

    @Test
    void fetch_nullUrl_returnsNull() {
        Document result = websiteFetcher.fetch(null);
        assertNull(result);
    }
}