package org.example;

import java.net.URI;
import java.util.List;

public class UrlValidator {

    private final List<String> targetDomains;

    public UrlValidator(List<String> targetDomains) {
        this.targetDomains = targetDomains;
    }

    public boolean isValid(String url) {
        try {
            var host = URI.create(url).getHost();
            if (host == null) {
                System.out.printf("%n%s has no host component%n", url);
                return false;
            }
            return targetDomains.stream()
                    .anyMatch(domain -> host.toLowerCase().contains(domain.toLowerCase()));
        } catch (IllegalArgumentException e) {
            System.out.printf("%n%s is not a valid URL%n", url);
            return false;
        }
    }
}
