package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        try {
            java.lang.reflect.Method method = Utilities.class.getDeclaredMethod("sanitizeUrl", String.class);
            method.setAccessible(true);
            assertEquals(expected, method.invoke(null, input));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testCreateMarkdownIndentation_variousDepths() {
        try {
            java.lang.reflect.Method method = Utilities.class.getDeclaredMethod("createMarkdownIndentation", int.class);
            method.setAccessible(true);
            assertEquals("", method.invoke(null, 0));
            assertEquals("-->", method.invoke(null, 1));
            assertEquals("---->", method.invoke(null, 2));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
