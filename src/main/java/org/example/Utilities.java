package org.example;

public class Utilities {
    public static String sanitizeUrl(String url) {
        if (url.contains("#")) {
            return url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    public static String createMarkdownIndentation(int depth) {
        return "--".repeat(depth) + (depth > 0 ? ">" : "");
    }

}
