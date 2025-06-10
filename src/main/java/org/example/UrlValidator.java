package org.example;

import org.example.utils.Logger;

import java.net.URI;
import java.util.List;

public class UrlValidator {

    private final List<String> targetDomains;
    private final Logger logger = Logger.getInstance();

    public UrlValidator(List<String> targetDomains) {
        this.targetDomains = targetDomains;
    }

    public boolean isValid(String url) {
        try {
            var host = URI.create(url).getHost();
            if (host == null) {
                logger.debug("%s has no host component", url);
                return false;
            }
            return targetDomains.stream()
                    .anyMatch(domain -> host.toLowerCase().contains(domain.toLowerCase()));
        } catch (IllegalArgumentException e) {
            logger.debug("%s is not a valid URL", url);
            return false;
        }
    }
}
