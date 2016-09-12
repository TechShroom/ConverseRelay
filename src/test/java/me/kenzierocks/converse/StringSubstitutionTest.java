package me.kenzierocks.converse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import me.kenzierocks.converse.util.StringSubstitution;

public class StringSubstitutionTest {

    @Test
    public void noSubstitutionsAndEmptyMap() {
        String input = "foobar";
        String expect = input;
        Map<String, String> map = ImmutableMap.of();
        assertEqual(expect, input, map);
    }

    @Test
    public void noSubstitutionsAndNonEmptyMap() {
        String input = "foobar";
        String expect = input;
        Map<String, String> map = ImmutableMap.of("foo", "bat");
        assertEqual(expect, input, map);
    }

    @Test
    public void oneSubstitutionAndEmptyMap() {
        String input = "it's a ${foobar} of life";
        Map<String, String> map = ImmutableMap.of();
        assertMissingKey(input, map);
    }

    @Test
    public void oneSubstitutionAndNonEmptyMap() {
        String input = "it's a ${foobar} of life";
        String expected = "it's a circle of life";
        Map<String, String> map = ImmutableMap.of("foobar", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void nonStandardSubstitutionAndNonEmptyMap() {
        String input = "it's a ${foo$bar} of life";
        String expected = "it's a circle of life";
        Map<String, String> map = ImmutableMap.of("foo$bar", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void missingEndOfKey() {
        String input = "it's a ${foo";
        String expected = "it's a ${foo";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void noStartOfKey() {
        String input = "it's a foo}";
        String expected = "it's a foo}";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void escapedDollarSign() {
        String input = "it's a \\${foo}";
        String expected = "it's a ${foo}";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void escapedStart() {
        String input = "it's a $\\{foo}";
        String expected = "it's a ${foo}";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void escapedEnd() {
        String input = "it's a ${foo\\}";
        String expected = "it's a ${foo}";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void escapedBackslash() {
        String input = "it's a \\\\${foo}";
        String expected = "it's a \\circle";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void backslash() {
        String input = "it's \\a ${foo}";
        String expected = "it's \\a circle";
        Map<String, String> map = ImmutableMap.of("foo", "circle");
        assertEqual(expected, input, map);
    }

    @Test
    public void emptyString() {
        String input = "";
        String expected = "";
        Map<String, String> map = ImmutableMap.of();
        assertEqual(expected, input, map);
    }

    @Test
    public void nonBMPCodePoints() {
        // \uD83C\uDDFA\uD83C\uDDF8 -> American flag
        String input = "${productName\uD83C\uDDFA\uD83C\uDDF8}™";
        String expected = "ConverseRelay™";
        Map<String, String> map = ImmutableMap.of("productName\uD83C\uDDFA\uD83C\uDDF8", "ConverseRelay");
        assertEqual(expected, input, map);
    }

    private void assertMissingKey(String format, Map<String, String> replacements) {
        try {
            String result = StringSubstitution.formatting(format).substitute(replacements);
            fail("unexpected result " + result);
        } catch (IllegalStateException failedSubstitute) {
            String start = "Missing required key";
            assertTrue("doesn't start with " + start + ": " + failedSubstitute.getLocalizedMessage(),
                    failedSubstitute.getLocalizedMessage().startsWith(start));
        }
    }

    private void assertEqual(String expect, String format, Map<String, String> replacements) {
        String result = StringSubstitution.formatting(format).substitute(replacements);
        assertEquals(expect, result);
    }

}
