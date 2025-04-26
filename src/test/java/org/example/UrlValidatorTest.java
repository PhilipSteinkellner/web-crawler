package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    private UrlValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new UrlValidator(List.of("example.com", "test.org"));
    }

    @Test
    void testValidUrlWithTargetDomain() {
        assertTrue(urlValidator.isValid("http://example.com"));
        assertTrue(urlValidator.isValid("https://subdomain.example.com"));
        assertTrue(urlValidator.isValid("http://www.test.org/page"));
    }

    @Test
    void testInvalidUrlWithNonTargetDomain() {
        assertFalse(urlValidator.isValid("http://anotherdomain.com"));
        assertFalse(urlValidator.isValid("https://random.org"));
    }

    @Test
    void testMalformedUrl() {
        assertFalse(urlValidator.isValid("invalidurl"));
        assertFalse(urlValidator.isValid("http://"));
        assertFalse(urlValidator.isValid("://missing-schema.com"));
    }

    @Test
    void testUrlWithNoHost() {
        assertFalse(urlValidator.isValid("file:///path/to/file.txt"));
    }

    @Test
    void testNullUrl() {
        assertThrows(NullPointerException.class, () -> urlValidator.isValid(null));
    }

    @Test
    void testEmptyUrl() {
        assertFalse(urlValidator.isValid(""));
        assertFalse(urlValidator.isValid("   ")); // Nur Leerzeichen
    }

    @Test
    void testCaseInsensitiveDomainMatching() {
        assertTrue(urlValidator.isValid("http://EXAMPLE.com/something"));
        assertTrue(urlValidator.isValid("https://www.TEST.org/index"));
    }

    @Test
    void testSubdomainMatching() {
        assertTrue(urlValidator.isValid("http://sub.example.com"));
        assertTrue(urlValidator.isValid("http://deep.test.org/path"));
    }

    @Test
    void testEmptyTargetDomains() {
        UrlValidator emptyValidator = new UrlValidator(List.of());
        assertFalse(emptyValidator.isValid("http://example.com"));
    }
    @Test
    void testNonMatchingDomain() {
        assertFalse(urlValidator.isValid("http://otherdomain.com"));
    }

    @Test
    void testInvalidUrl() {
        assertFalse(urlValidator.isValid("not_a_valid_url"));
    }

    @Test
    void testExactMatchingDomain() {
        assertTrue(urlValidator.isValid("http://example.com"));
    }
}