package org.example;

import org.example.website.Heading;
import org.example.website.Link;
import org.example.website.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebsiteFetcher {

    private final Logger logger = Logger.getInstance();

    public Page fetchPage(String url, int depth) {
        if (url == null || url.trim().isEmpty()) {
            return new Page(url, depth, true, Collections.emptyList(), Collections.emptyList());
        }
        try {
            Document doc = Jsoup.connect(url).get();
            return buildPage(url, depth, doc);
        } catch (IOException e) {
            return new Page(url, depth, true, Collections.emptyList(), Collections.emptyList());
        }
    }

    public Page buildPage(String url, int depth, Document doc) {
        Elements docLinks = doc.select("a[href]");
        List<Link> websiteLinks = new ArrayList<>();

        for (Element docLink : docLinks) {
            String href = docLink.attr("abs:href");
            websiteLinks.add(new Link(href));
        }

        Elements docHeadings = doc.select("h1, h2, h3, h4, h5, h6");
        List<Heading> websiteHeadings = new ArrayList<>();

        for (Element docHeading : docHeadings) {
            String tagName = docHeading.tagName();
            String text = docHeading.text();
            websiteHeadings.add(new Heading(tagName, text));
        }

        return new Page(url, depth, false, websiteHeadings, websiteLinks);
    }
}
