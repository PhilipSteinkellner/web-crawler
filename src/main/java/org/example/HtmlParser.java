package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlParser {

    private static void print(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }

    public void listLinks(String url) throws IOException {
        print("\nFetching %s...", url);

        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

//        print("\nMedia: (%d)", media.size());
//        for (Element src : media) {
//            if (src.nameIs("img"))
//                print(" * %s: <%s> %sx%s (%s)",
//                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
//                        trim(src.attr("alt"), 20));
//            else
//                print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
//        }

//        print("\nImports: (%d)", imports.size());
//        for (Element link : imports) {
//            print(" * %s <%s> (%s)", link.tagName(), link.attr("abs:href"), link.attr("rel"));
//        }

        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        }
    }

    public String parse(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        System.out.printf("\n" + doc.title());
        Elements newsHeadlines = doc.select("#mp-itn b a");
        for (Element headline : newsHeadlines) {
            System.out.printf("\n%s - %s", headline.attr("title"), headline.absUrl("href"));
        }
        return "";
    }
}
