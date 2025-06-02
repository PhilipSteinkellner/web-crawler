package org.example;

import org.example.website.Link;
import org.example.website.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebsiteAnalyzer {

    private final UrlValidator urlValidator;
    private final int maxDepth;
    private final Set<String> seenUrls = ConcurrentHashMap.newKeySet();
    private final List<Page> pages = Collections.synchronizedList(new ArrayList<>());
    private final Logger logger = Logger.getInstance();
    protected WebsiteFetcher websiteFetcher;
    protected MarkdownRecorder markdownRecorder;

    public WebsiteAnalyzer(List<String> targetDomains, int maxDepth, MarkdownFileWriter markdownFileWriter) {
        this.urlValidator = new UrlValidator(targetDomains);
        this.websiteFetcher = new WebsiteFetcher();
        this.markdownRecorder = new MarkdownRecorder(markdownFileWriter);
        this.maxDepth = maxDepth;
    }

    private static String sanitizeUrl(String url) {
        if (url.contains("#")) {
            return url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    private static String createMarkdownIndentation(int depth) {
        return "--".repeat(depth) + (depth > 0 ? ">" : "");
    }

    public void recordInputArguments(String url, List<String> targetDomains, int maxDepth) throws IOException {
        this.markdownRecorder.recordInputArguments(url, targetDomains, maxDepth);
    }

    public void startAnalysis(String url) throws IOException {
        WebCrawlerEngine crawler = new WebCrawlerEngine(10, this);
        crawler.crawl(url, 0, maxDepth);
        writeReport();
    }

    public List<Link> analyze(String url, int depth) throws IOException {
        url = sanitizeUrl(url);

        if (!seenUrls.add(url)) return null;
        if (depth > 0 && !urlValidator.isValid(url)) return null;

        Page page = websiteFetcher.fetchPage(url, depth);
        pages.add(page);

        String markdownIndentation = createMarkdownIndentation(page.depth());

        if (page.broken()) {
            logger.info("%s %s: broken link", markdownIndentation, url);

            return null;
        }

        logger.info("%s %s: Found %d headings, %d links", markdownIndentation, url, page.headings().size(), page.links().size());

        return page.links();
    }

    void writeReport() throws IOException {
        for (Page page : pages) {
            String markdownIndentation = createMarkdownIndentation(page.depth());

            if (page.depth() > 0) {
                if (page.broken()) {
                    markdownRecorder.recordBrokenLink(page.url(), markdownIndentation);
                } else {
                    markdownRecorder.recordLink(page.url(), markdownIndentation);
                }
            }

            markdownRecorder.recordHeadings(page.headings(), markdownIndentation);
        }
    }
}
