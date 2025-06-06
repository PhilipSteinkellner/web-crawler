package org.example;

import org.example.website.Heading;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRecorder {

    private final MarkdownWriter markdownWriter;

    public MarkdownRecorder(MarkdownWriter markdownWriter) {
        this.markdownWriter = markdownWriter;
    }

    public void recordInputArguments(String url, List<String> targetDomains, int maxDepth) throws IOException {
        String content = ("**Input Arguments**") +
                String.format("%n- URL: %s", url) +
                String.format("%n- Domains: %s", String.join(", ", targetDomains)) +
                String.format("%n- Depth: %d%n", maxDepth);

        markdownWriter.write(content);
    }

    public void recordBrokenLink(String url, String indentation) throws IOException {
        markdownWriter.write(String.format("%n%n%s broken link %s%n", indentation, url));
    }

    public void recordLink(String url, String indentation) throws IOException {
        markdownWriter.write(String.format("%n%n%s link to %s%n", indentation, url));
    }

    public void recordHeadings(List<Heading> headings, String indentation) throws IOException {
        Pattern pattern = Pattern.compile("\\d+");
        for (Heading heading : headings) {
            String headingPrefix = "#";
            Matcher matcher = pattern.matcher(heading.tagName());
            if (matcher.find()) {
                int headingNumber = Integer.parseInt(matcher.group());
                headingPrefix = headingPrefix.repeat(headingNumber);
            }
            String line = String.format("%n%s %s %s", headingPrefix, indentation, heading.text());
            markdownWriter.write(line);
        }
    }

}
