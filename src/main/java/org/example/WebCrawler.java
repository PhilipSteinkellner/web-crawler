package org.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "web-crawler", mixinStandardHelpOptions = true, version = "web-crawler 1.0",
        description = "Provides a compact overview of the given website and linked websites by only listing the headings and the links.")
class WebCrawler implements Callable<Integer> {

    private final HtmlParser htmlParser = new HtmlParser();

    @Parameters(index = "0", description = "The url to start from")
    private String url;

    @Parameters(index = "1..*", description = "The domains to consider")
    private List<String> domains;

    @Option(names = {"-d", "--depth"}, description = "The depth of websites to crawl")
    private int depth = 1;

    @Override
    public Integer call() throws Exception {
        System.out.printf("\nURL: %s", this.url);
        System.out.printf("\ndomains: %s", this.domains);
        System.out.printf("\ndepth: %s", this.depth);
        this.htmlParser.listLinks(url);
        return 0;
    }
}