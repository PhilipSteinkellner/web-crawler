package org.example;

import picocli.CommandLine;
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
    private int depth = 1;

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new WebCrawler()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        System.out.printf("URL: %s\n", this.url);
        System.out.printf("domains: %s\n", this.domains);
        System.out.printf("depth: %s", this.depth);
        return 0;
    }
}