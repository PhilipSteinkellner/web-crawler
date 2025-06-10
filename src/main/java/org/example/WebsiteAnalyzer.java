package org.example;

import org.example.utils.Logger;
import org.example.utils.Utilities;
import org.example.website.Link;
import org.example.website.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebsiteAnalyzer {

    private static final int THREAD_COUNT = 10;
    private final UrlValidator urlValidator;
    private final int maxDepth;
    private final Set<String> seenUrls = ConcurrentHashMap.newKeySet();
    final List<Page> pages = Collections.synchronizedList(new ArrayList<>());
    private final Logger logger = Logger.getInstance();
    protected WebsiteFetcher websiteFetcher;
    protected MarkdownRecorder markdownRecorder;

    public WebsiteAnalyzer(List<String> targetDomains, int maxDepth, MarkdownFileWriter markdownFileWriter) {
        this.urlValidator = new UrlValidator(targetDomains);
        this.websiteFetcher = new WebsiteFetcher();
        this.markdownRecorder = new MarkdownRecorder(markdownFileWriter);
        this.maxDepth = maxDepth;
    }

    public void recordInputArguments(String url, List<String> targetDomains, int maxDepth) throws IOException {
        this.markdownRecorder.recordInputArguments(url, targetDomains, maxDepth);
    }

    public void startAnalysis(String url) throws IOException {
        logger.info("Starting analysis... ");
        WebCrawlerEngine crawler = new WebCrawlerEngine(THREAD_COUNT, this);
        String urlSanitized = Utilities.sanitizeUrl(url);
        crawler.crawl(urlSanitized, 0, maxDepth);
        logger.info("Finished analysis. Analyzed %d pages.", pages.size());
        writeReport();
    }

    public List<Link> analyze(String url, int depth) {
        if (url == null || url.isEmpty() || depth > maxDepth) {
            return Collections.emptyList();
        }

        if (!seenUrls.add(url)) return Collections.emptyList();
        if (depth > 0 && !urlValidator.isValid(url)) return Collections.emptyList();

        Page page = websiteFetcher.fetchPage(url, depth);
        pages.add(page);

        String markdownIndentation = Utilities.createMarkdownIndentation(page.depth());

        if (page.broken()) {
            logger.debug("%s %s: broken link", markdownIndentation, url);

            return Collections.emptyList();
        }

        logger.debug("%s %s: Found %d headings, %d links", markdownIndentation, url, page.headings().size(), page.links().size());

        return page.links();
    }

    void writeReport() throws IOException {
        if (pages.isEmpty()) {
            logger.warn("No pages analyzed - report skipped");
            return;
        }

        Page rootPage = pages.remove(0);
        writePageResult(rootPage);
    }


    private void writePageResult(Page page) throws IOException {
        if (page == null) return;
        markdownRecorder.recordHeadings(page.headings(), Utilities.createMarkdownIndentation(page.depth()));
        if (page.broken()) {
            markdownRecorder.recordBrokenLink(page.url(), Utilities.createMarkdownIndentation(page.depth()));
        } else {
            for (Link link : page.links()) {
                markdownRecorder.recordLink(link.href(), Utilities.createMarkdownIndentation(page.depth() + 1));
                int linkedPageIndex = Utilities.indexOf(pages, item -> item.url().equals(link.href()) && item.depth() == page.depth() + 1);
                if (linkedPageIndex > -1) {
                    Page linkedPage = pages.remove(linkedPageIndex);
                    writePageResult(linkedPage);
                }
            }
        }

    }
}
