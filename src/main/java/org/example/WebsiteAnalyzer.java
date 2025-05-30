package org.example;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        String markdownIndentation = createMarkdownIndentation(depth);

        if (depth > 0 && !urlValidator.isValid(url)) {
            return;
        }

        logger.info("Analyzing %s", url);

        Document doc = websiteFetcher.fetch(url);
        if (doc == null) {
            markdownRecorder.recordBrokenLink(url, markdownIndentation);
            return;
        }

        Elements links = doc.select("a[href]");
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        logger.info("Found %d headings, %d links", headings.size(), links.size());

        if (depth > 0) {
            markdownRecorder.recordLink(url, markdownIndentation);
        }

        markdownRecorder.recordHeadings(headings, markdownIndentation);

        for (Element link : links) {
            String href = link.attr("abs:href");
            analyze(href, depth + 1);
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
