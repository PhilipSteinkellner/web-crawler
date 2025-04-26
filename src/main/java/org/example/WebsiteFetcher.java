package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WebsiteFetcher {

    public Document fetch(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
    }
}
