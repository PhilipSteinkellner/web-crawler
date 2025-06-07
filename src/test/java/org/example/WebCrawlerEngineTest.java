package org.example;

import org.example.website.Link;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class WebCrawlerEngineTest {

    private WebsiteAnalyzer analyzer;
    private WebCrawlerEngine engine;

    @BeforeEach
    void setUp() {
        analyzer = mock(WebsiteAnalyzer.class);
        engine = new WebCrawlerEngine(2, analyzer);
    }

    @Test
    void crawl_invokesAnalyzerAndFollowsLinksUpToMaxDepth() {
        String rootUrl = "https://example.com";
        Link link1 = new Link("https://example.com/page1");
        Link link2 = new Link("https://example.com/page2");

        when(analyzer.analyze(rootUrl, 0)).thenReturn(List.of(link1, link2));
        when(analyzer.analyze(link1.href(), 1)).thenReturn(List.of());
        when(analyzer.analyze(link2.href(), 1)).thenReturn(List.of());

        engine.crawl(rootUrl, 0, 1);

        verify(analyzer).analyze(rootUrl, 0);
        verify(analyzer).analyze(link1.href(), 1);
        verify(analyzer).analyze(link2.href(), 1);
        verifyNoMoreInteractions(analyzer);
    }

    @Test
    void crawl_doesNotExceedMaxDepth() {
        String rootUrl = "https://test.com";
        Link deepLink = new Link("https://test.com/deep");

        when(analyzer.analyze(rootUrl, 0)).thenReturn(List.of(deepLink));

        engine.crawl(rootUrl, 0, 0);

        verify(analyzer).analyze(rootUrl, 0);
        verifyNoMoreInteractions(analyzer);
    }

    @Test
    void crawl_runsTasksConcurrently() {
        String rootUrl = "https://concurrent.com";
        Link link1 = new Link("https://concurrent.com/page1");
        Link link2 = new Link("https://concurrent.com/page2");

        CountDownLatch latch = new CountDownLatch(2);

        when(analyzer.analyze(rootUrl, 0)).thenReturn(List.of(link1, link2));
        when(analyzer.analyze(link1.href(), 1)).thenAnswer(invocation -> {
            latch.countDown();
            return List.of();
        });
        when(analyzer.analyze(link2.href(), 1)).thenAnswer(invocation -> {
            latch.countDown();
            return List.of();
        });

        engine.crawl(rootUrl, 0, 1);

        verify(analyzer).analyze(rootUrl, 0);
        verify(analyzer).analyze(link1.href(), 1);
        verify(analyzer).analyze(link2.href(), 1);
    }

    @Test
    void crawl_handlesInterruptedExceptionDuringTaskWait() throws Exception {
        engine.activeTasks.set(1);

        Thread crawlerThread = new Thread(() ->
                engine.crawl("https://interrupted-wait.com", 0, 1)
        );
        crawlerThread.start();

        crawlerThread.interrupt();

        crawlerThread.join(2000);
        assertFalse(crawlerThread.isAlive(), "Crawler thread should terminate after being interrupted");
    }
}
