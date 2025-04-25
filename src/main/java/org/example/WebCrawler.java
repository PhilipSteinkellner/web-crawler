package org.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "web-crawler", mixinStandardHelpOptions = true, version = "web-crawler 1.0",
        description = "Provides a compact overview of the given website and linked websites by only listing the headings and the links.")
public class WebCrawler implements Callable<Integer> {

    @Parameters(index = "0", description = "The url to start from")
    private String url;

    @Parameters(index = "1..*", description = "The domains to consider")
    private List<String> targetDomains;

    @Option(names = {"-d", "--depth"}, description = "The maximum depth of websites to crawl")
    private int maxDepth = 0; // Refactored: Removed static final and renamed to maxDepth

    @Override
    public Integer call() throws Exception {
        MarkdownFileWriter markdownFileWriter = new MarkdownFileWriter("report.md");

        String content = ("**Input Arguments**") +
                String.format("%n- URL: <a>%s</a>", url) +
                String.format("%n- Domains: %s", String.join(", ", targetDomains)) +
                String.format("%n- Depth: <a>%d</a>", maxDepth);

        markdownFileWriter.write(content);

        WebsiteAnalyzer websiteAnalyzer = new WebsiteAnalyzer(targetDomains, maxDepth, markdownFileWriter);

        websiteAnalyzer.analyze(url, 0);

        return 0;
    }
}