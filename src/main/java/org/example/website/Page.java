package org.example.website;

import java.util.List;

public record Page(String url, int depth, boolean broken, List<Heading> headings, List<Link> links) {
}
