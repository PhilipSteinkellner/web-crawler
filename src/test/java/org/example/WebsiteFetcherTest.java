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

    public static final String TEST_URL = "https://example.com";
    private WebsiteFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new WebsiteFetcher();
    }

    @Test
    void fetch_Page_returnsNull_whenUrlIsNull() {
        assertNull(fetcher.fetchPage(null));
    }

    @Test
    void fetch_Page_returnsNull_whenUrlIsEmpty() {
        assertNull(fetcher.fetchPage(""));
    }

    @Test
    void fetch_returnsDocument_whenFetchPageSuccessful() throws IOException {
        Document mockDocument = mock(Document.class);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);

            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenReturn(mockDocument);

            Document result = fetcher.fetchPage(TEST_URL);

            assertNotNull(result);
            assertEquals(mockDocument, result);
            jsoupMock.verify(() -> Jsoup.connect(TEST_URL));
            verify(connectionMock).get();
        }
    }

    @Test
    void fetch_Page_returnsNull_whenIOExceptionOccurs() throws IOException {

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class);

            jsoupMock.when(() -> Jsoup.connect(TEST_URL)).thenReturn(connectionMock);
            when(connectionMock.get()).thenThrow(new IOException("Connection failed"));

            Document result = fetcher.fetchPage(TEST_URL);

            assertNull(result);
            jsoupMock.verify(() -> Jsoup.connect(TEST_URL));
            verify(connectionMock).get();
        }
    }
}
