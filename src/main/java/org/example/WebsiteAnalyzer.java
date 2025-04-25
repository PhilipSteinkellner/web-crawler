package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteAnalyzer {

    private final List<String> domains;
    private final int maxDepth;
    private final MarkdownFileWriter markdownFileWriter;
    private final Set<String> seenUrls = new HashSet<>();

    public WebsiteAnalyzer(List<String> domains, int maxDepth, MarkdownFileWriter markdownFileWriter) {
        this.domains = domains;
        this.maxDepth = maxDepth;
        this.markdownFileWriter = markdownFileWriter;
    }

    private static void print(String msg, Object... args) {
        System.out.printf("%s%n", String.format(msg, args));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private boolean checkHostValidity(String url) {
        try {
            var host = URI.create(url).getHost();
            if (host == null) {
                print("\n%s has no host component", url);
                return false;
            }

            return domains.stream()
                    .anyMatch(s -> host.toLowerCase().contains(s.toLowerCase()));
        } catch (IllegalArgumentException e) {
            print("\n%s is not a valid URL", url);
            return false;
        }
    }


    public Document fetchAndParseWebsite(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
    }

    public void recordHeadings(Elements headings, String markdownIndentation) throws IOException {
        Pattern pattern = Pattern.compile("\\d+");

        for (Element heading : headings) {
            String headingPrefix = "#";
            Matcher matcher = pattern.matcher(heading.tag().getName());
            if (matcher.find()) {
                int headingNumber = Integer.parseInt(matcher.group());
                headingPrefix = headingPrefix.repeat(headingNumber);
            }
            String line = String.format("%n%s %s %s", headingPrefix, markdownIndentation, heading.text());
            markdownFileWriter.write(line);
        }
    }

    public void analyze(String url, int depth) throws IOException {
        if (depth > maxDepth) return;

        if (url.contains("#")) url = url.substring(0, url.indexOf("#"));

        if (seenUrls.contains(url)) return;
        else seenUrls.add(url);

        String indentation = "--".repeat(depth);
        if (!indentation.isEmpty()) indentation += ">";

        if (depth > 0) {
            boolean validHost = checkHostValidity(url);
            if (!validHost) return;
        }

        print("\nAnalyzing %s", url);

        Document doc = fetchAndParseWebsite(url);
        if (doc == null) {
            markdownFileWriter.write(String.format("%n%n<br>%s broken link <a>%s</a>%n", indentation, url));
            return;
        }

        Elements links = doc.select("a[href]");
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        print("\nFound %d headings, %d links", headings.size(), links.size());

        if (depth > 0) {
            markdownFileWriter.write(String.format("%n%n<br>%s link to <a>%s</a>%n", indentation, url));
        }

        this.recordHeadings(headings, indentation);

        for (Element link : links) {
            String href = link.attr("abs:href");
            analyze(href, depth + 1);
        }
    }
}
