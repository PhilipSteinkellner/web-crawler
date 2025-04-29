package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WebsiteFetcher {

    private final Logger logger = Logger.getInstance();

    public Document fetch(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            logger.debug("broken link %s", url);
            return null;
        }
    }
}
