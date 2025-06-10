package org.example;

import org.example.utils.Utilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilitiesTest {

    @ParameterizedTest
    @CsvSource({
            "https://example.com/page#section, https://example.com/page",
            "https://example.com/page, https://example.com/page",
            "https://example.com/page#, https://example.com/page",
            "'', ''",
            "#section, ''"
    })

    void sanitizeUrl_shouldReturnExpected(String input, String expected) {
        assertEquals(expected, Utilities.sanitizeUrl(input));
    }

    @Test
    void testCreateMarkdownIndentation_variousDepths() {
        assertEquals("", Utilities.createMarkdownIndentation(0));
        assertEquals("-->", Utilities.createMarkdownIndentation(1));
        assertEquals("---->", Utilities.createMarkdownIndentation(2));
    }

    @Test
    void sanitizeUrl_HandlesNullInput() {
        String result = Utilities.sanitizeUrl(null);
        assertEquals("", result);
    }

}
