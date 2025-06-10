package org.example;

import java.util.List;
import java.util.function.Predicate;

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

    public static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
