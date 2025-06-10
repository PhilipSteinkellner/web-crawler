package org.example;

import org.example.website.Link;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawlerEngine {
    final ExecutorService executor;
    final AtomicInteger activeTasks = new AtomicInteger();
    final Object taskLock = new Object();
    final WebsiteAnalyzer analyzer;

    public WebCrawlerEngine(int threadCount, WebsiteAnalyzer analyzer) {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.analyzer = analyzer;
    }

    public void crawl(String url, int depth, int maxDepth) {
        submitRecursive(url, depth, maxDepth);

        synchronized (taskLock) {
            while (activeTasks.get() > 0) {
                try {
                    taskLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void submitRecursive(String url, int depth, int maxDepth) {
        activeTasks.incrementAndGet();
        executor.submit(() -> {
            try {
                List<Link> links = analyzer.analyze(url, depth);
                if (depth < maxDepth) {
                    for (Link link : links) {
                        submitRecursive(link.href(), depth + 1, maxDepth);
                    }
                }
            } finally {
                if (activeTasks.decrementAndGet() == 0) {
                    synchronized (taskLock) {
                        taskLock.notifyAll();
                    }
                }
            }
        });
    }
}
