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
import java.util.List;

public class WebsiteFetcher {

    private final Logger logger = Logger.getInstance();

    public Page fetchPage(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            Document doc = Jsoup.connect(url).get();

            return buildPage(doc);
        } catch (IOException e) {
            logger.debug("broken link %s", url);
            return null;
        }
    }

    public Page buildPage(Document doc) {
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

        return new Page(websiteHeadings, websiteLinks);
    }
}
