package com.leetmate.platform.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Pure utility to approximate cyclomatic complexity for submitted code.
 */
@Component
public class CyclomaticComplexityCalculator {

    private static final Pattern BRANCH_KEYWORDS = Pattern.compile("\\b(if|for|while|case|catch)\\b");
    private static final Pattern AND_PATTERN = Pattern.compile("&&");
    private static final Pattern OR_PATTERN = Pattern.compile("\\|\\|");
    private static final Pattern TERNARY_PATTERN = Pattern.compile("\\?");

    /**
     * Estimates the cyclomatic complexity. Starts at 1 and increments for common branching constructs.
     *
     * @param code user supplied code
     * @return estimated cyclomatic complexity
     */
    public int calculate(String code) {
        if (code == null || code.isBlank()) {
            return 1;
        }
        String sanitized = stripCommentsAndStrings(code);
        int complexity = 1;
        complexity += countMatches(BRANCH_KEYWORDS, sanitized);
        complexity += countMatches(AND_PATTERN, sanitized);
        complexity += countMatches(OR_PATTERN, sanitized);
        complexity += countMatches(TERNARY_PATTERN, sanitized);
        return complexity;
    }

    private int countMatches(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        int occurrences = 0;
        while (matcher.find()) {
            occurrences++;
        }
        return occurrences;
    }

    private String stripCommentsAndStrings(String code) {
        Objects.requireNonNull(code, "code");
        StringBuilder builder = new StringBuilder(code.length());
        boolean inSingleComment = false;
        boolean inMultiComment = false;
        boolean inString = false;
        boolean inChar = false;
        for (int i = 0; i < code.length(); i++) {
            char current = code.charAt(i);
            char next = i + 1 < code.length() ? code.charAt(i + 1) : '\0';

            if (inSingleComment) {
                if (current == '\n') {
                    inSingleComment = false;
                    builder.append(current);
                }
                continue;
            }
            if (inMultiComment) {
                if (current == '*' && next == '/') {
                    inMultiComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (current == '\\' && next != '\0') {
                    i++;
                } else if (current == '"') {
                    inString = false;
                }
                builder.append(' ');
                continue;
            }
            if (inChar) {
                if (current == '\\' && next != '\0') {
                    i++;
                } else if (current == '\'') {
                    inChar = false;
                }
                builder.append(' ');
                continue;
            }

            if (current == '/' && next == '/') {
                inSingleComment = true;
                i++;
                continue;
            }
            if (current == '/' && next == '*') {
                inMultiComment = true;
                i++;
                continue;
            }
            if (current == '"') {
                inString = true;
                builder.append(' ');
                continue;
            }
            if (current == '\'') {
                inChar = true;
                builder.append(' ');
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }
}
