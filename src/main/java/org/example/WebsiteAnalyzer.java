package org.example;

import org.example.website.Link;
import org.example.website.Page;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebsiteAnalyzer {

    private final UrlValidator urlValidator;
    private final int maxDepth;
    private final Set<String> seenUrls = new HashSet<>();
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

    public void analyze(String url, int depth) throws IOException {
        if (depth > maxDepth) return;

        url = sanitizeUrl(url);

        if (seenUrls.contains(url)) return;
        seenUrls.add(url);

        if (depth > 0 && !urlValidator.isValid(url)) {
            return;
        }

        logger.info("Analyzing %s", url);

        String markdownIndentation = createMarkdownIndentation(depth);

        Page page = websiteFetcher.fetchPage(url);

        if (page == null) {
            markdownRecorder.recordBrokenLink(url, markdownIndentation);
            return;
        }

        logger.info("Found %d headings, %d links", page.headings().size(), page.links().size());

        if (depth > 0) {
            markdownRecorder.recordLink(url, markdownIndentation);
        }

        markdownRecorder.recordHeadings(page.headings(), markdownIndentation);

        for (Link link : page.links()) {
            analyze(link.href(), depth + 1);
        }
    }

    String sanitizeUrl(String url) {
        if (url.contains("#")) {
            return url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    String createMarkdownIndentation(int depth) {
        String indentation = "--".repeat(depth);
        return indentation.isEmpty() ? indentation : indentation + ">";
    }
}
