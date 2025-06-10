package org.example.utils;

import java.util.List;
import java.util.function.Predicate;

public class Utilities {

    private Utilities() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Removes the fragment part (after '#') from a URL string or returns an empty string if input is null or empty.
     */
    public static String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int hashIndex = url.indexOf('#');
        return hashIndex > -1 ? url.substring(0, hashIndex) : url;
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
