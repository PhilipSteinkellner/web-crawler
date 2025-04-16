package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {

    private final List<String> domains;
    private final int maxDepth;
    private final FileWriter fileWriter;
    private final Set<String> seenUrls = new HashSet<>();

    public HtmlParser(List<String> domains, int maxDepth, FileWriter fileWriter) {
        this.domains = domains;
        this.maxDepth = maxDepth;
        this.fileWriter = fileWriter;
    }

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public void analyze(String url, int depth) throws IOException {
        if (depth > this.maxDepth) return;

        if (url.contains("#")) url = url.substring(0, url.indexOf("#"));

        if (seenUrls.contains(url)) return;
        else seenUrls.add(url);

        String indentation = "--".repeat(depth);
        if (!indentation.isEmpty()) indentation += ">";

        try {
            var host = new URL(url).getHost();
            boolean validHost = domains.stream()
                    .anyMatch(s -> host.toLowerCase().contains(s.toLowerCase()));
            if (!validHost) return;
        } catch (MalformedURLException e) {
            print("\n%s is not a valid URL", url);
            return;
        }


        print("\nAnalysing %s", url);

        Document doc;
        try {
            doc = Jsoup.connect(url).get();

        } catch (IOException e) {
            fileWriter.write(String.format("\n\n<br>%s broken link <a>%s</a>\n", indentation, url));
            return;
        }

        Elements links = doc.select("a[href]");
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        print("\n Found %d headings, %d links", headings.size(), links.size());

        fileWriter.write(String.format("\n\n<br>%s link to <a>%s</a>\n", indentation, url));

        Pattern pattern = Pattern.compile("\\d+");

        for (Element heading : headings) {
            String headingPrefix = "#";
            Matcher matcher = pattern.matcher(heading.tag().getName());
            if (matcher.find()) {
                int headingNumber = Integer.parseInt(matcher.group());
                headingPrefix = headingPrefix.repeat(headingNumber);
            }
            String line = String.format("\n%s %s %s", headingPrefix, indentation, heading.text());
            fileWriter.write(line);
        }

        for (Element link : links) {
            String href = link.attr("abs:href");
            this.analyze(href, depth + 1);
        }
    }
}
