package org.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "web-crawler", mixinStandardHelpOptions = true, version = "web-crawler 1.0",
        description = "Provides a compact overview of the given website and linked websites by only listing the headings and the links.")
class WebCrawler implements Callable<Integer> {

    @Parameters(index = "0", description = "The url to start from")
    private String url;

    @Parameters(index = "1..*", description = "The domains to consider")
    private List<String> domains;

    @Option(names = {"-d", "--depth"}, description = "The depth of websites to crawl")
    private int depth = 0;

    @Override
    public Integer call() throws Exception {
        FileWriter fileWriter = new FileWriter("report.md");

        String content = ("**Input Arguments**") +
                String.format("\n- URL: <a>%s</a>", url) +
                String.format("\n- Domains: %s", String.join(", ", domains)) +
                String.format("\n- Depth: <a>%d</a>", depth);

        fileWriter.write(content);

        WebsiteAnalyzer websiteAnalyzer = new WebsiteAnalyzer(domains, depth, fileWriter);

        websiteAnalyzer.analyze(url, 0);

        return 0;
    }
}