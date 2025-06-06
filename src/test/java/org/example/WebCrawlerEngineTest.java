package org.example;

import org.example.website.Link;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void crawl_invokesAnalyzerAndFollowsLinksUpToMaxDepth() throws IOException {
        String rootUrl = "http://example.com";
        Link link1 = new Link("http://example.com/page1");
        Link link2 = new Link("http://example.com/page2");

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
    void crawl_doesNotExceedMaxDepth() throws IOException {
        String rootUrl = "http://test.com";
        Link deepLink = new Link("http://test.com/deep");

        when(analyzer.analyze(rootUrl, 0)).thenReturn(List.of(deepLink));

        engine.crawl(rootUrl, 0, 0);

        verify(analyzer).analyze(rootUrl, 0);
        verifyNoMoreInteractions(analyzer);
    }

    @Test
    void crawl_handlesAnalyzerIOExceptionGracefully() throws IOException {
        String rootUrl = "http://fail.com";
        when(analyzer.analyze(rootUrl, 0)).thenThrow(new IOException("Simulated failure"));

        engine.crawl(rootUrl, 0, 1);

        verify(analyzer).analyze(rootUrl, 0);
    }

    @Test
    void crawl_runsTasksConcurrently() throws IOException, InterruptedException {
        String rootUrl = "http://concurrent.com";
        Link link1 = new Link("http://concurrent.com/page1");
        Link link2 = new Link("http://concurrent.com/page2");

        CountDownLatch latch = new CountDownLatch(2);

        when(analyzer.analyze(rootUrl, 0)).thenReturn(List.of(link1, link2));
        when(analyzer.analyze(link1.href(), 1)).thenAnswer(invocation -> {
            latch.countDown();
            latch.await(2, TimeUnit.SECONDS);
            return List.of();
        });
        when(analyzer.analyze(link2.href(), 1)).thenAnswer(invocation -> {
            latch.countDown();
            latch.await(2, TimeUnit.SECONDS);
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
                engine.crawl("http://interrupted-wait.com", 0, 1)
        );
        crawlerThread.start();

        Thread.sleep(100);
        crawlerThread.interrupt();

        crawlerThread.join(2000);
        assertTrue(!crawlerThread.isAlive(), "Crawler thread should terminate after being interrupted");
    }
}
