package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    public static final String TEST_URL = "https://example.com";
    private UrlValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new UrlValidator(List.of("example.com", "test.org"));
    }

    @Test
    void testValidUrlWithTargetDomain() {
        assertTrue(urlValidator.isValid(TEST_URL));
        assertTrue(urlValidator.isValid("https://subdomain.example.com"));
        assertTrue(urlValidator.isValid("https://www.test.org/page"));
    }

    @Test
    void testInvalidUrlWithNonTargetDomain() {
        assertFalse(urlValidator.isValid("https://anotherdomain.com"));
        assertFalse(urlValidator.isValid("https://random.org"));
    }

    @Test
    void testMalformedUrl() {
        assertFalse(urlValidator.isValid("invalid_url"));
        assertFalse(urlValidator.isValid("https://"));
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
        assertTrue(urlValidator.isValid("https://EXAMPLE.com/something"));
        assertTrue(urlValidator.isValid("https://www.TEST.org/index"));
    }

    @Test
    void testSubdomainMatching() {
        assertTrue(urlValidator.isValid("https://sub.example.com"));
        assertTrue(urlValidator.isValid("https://deep.test.org/path"));
    }

    @Test
    void testEmptyTargetDomains() {
        UrlValidator emptyValidator = new UrlValidator(List.of());
        assertFalse(emptyValidator.isValid(TEST_URL));
    }

    @Test
    void testNonMatchingDomain() {
        assertFalse(urlValidator.isValid("https://otherdomain.com"));
    }

    @Test
    void testInvalidUrl() {
        assertFalse(urlValidator.isValid("not_a_valid_url"));
    }

    @Test
    void testExactMatchingDomain() {
        assertTrue(urlValidator.isValid(TEST_URL));
    }
}