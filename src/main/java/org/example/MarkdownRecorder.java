package org.example;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRecorder {

    private final MarkdownFileWriter markdownFileWriter;

    public MarkdownRecorder(MarkdownFileWriter markdownFileWriter) {
        this.markdownFileWriter = markdownFileWriter;
    }

    public void recordBrokenLink(String url, String indentation) throws IOException {
        markdownFileWriter.write(String.format("%n%n%s broken link %s%n", indentation, url));
    }

    public void recordLink(String url, String indentation) throws IOException {
        markdownFileWriter.write(String.format("%n%n%s link to %s%n", indentation, url));
    }

    public void recordHeadings(Elements headings, String indentation) throws IOException {
        Pattern pattern = Pattern.compile("\\d+");
        for (Element heading : headings) {
            String headingPrefix = "#";
            Matcher matcher = pattern.matcher(heading.tagName());
            if (matcher.find()) {
                int headingNumber = Integer.parseInt(matcher.group());
                headingPrefix = headingPrefix.repeat(headingNumber);
            }
            String line = String.format("%n%s %s %s", headingPrefix, indentation, heading.text());
            markdownFileWriter.write(line);
        }
    }
}
