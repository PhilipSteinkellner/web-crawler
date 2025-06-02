package org.example.website;

import java.util.List;

public record Page(List<Heading> headings, List<Link> links) {
}
