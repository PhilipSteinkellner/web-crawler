package org.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "web-crawler", mixinStandardHelpOptions = true, version = "web-crawler 1.0",
        description = "Provides a compact overview of the given website and linked websites by only listing the headings and the links.")
public class WebCrawler implements Callable<Integer> {

    @Parameters(index = "0", description = "The url to start from")
    private String url = "";

    @Parameters(index = "1..*", description = "The domains to consider")
    private List<String> targetDomains = new ArrayList<>();

    @Option(names = {"-d", "--depth"}, description = "The maximum depth of websites to crawl")
    private int maxDepth = 0;

    @Override
    public Integer call() throws Exception {
        MarkdownFileWriter markdownFileWriter = new MarkdownFileWriter("report.md");

        WebsiteAnalyzer websiteAnalyzer = new WebsiteAnalyzer(targetDomains, maxDepth, markdownFileWriter);

        websiteAnalyzer.recordInputArguments(url, targetDomains, maxDepth);

        websiteAnalyzer.startAnalysis(url);

        return 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDepth() {
        return maxDepth;
    }

    public void setDepth(int depth) {
        this.maxDepth = depth;
    }

    public List<String> getDomains() {
        return targetDomains;
    }

    public void setDomains(List<String> domains) {
        this.targetDomains = domains;
    }
}