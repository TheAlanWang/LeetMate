package com.leetmate.platform.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CyclomaticComplexityCalculatorTest {

    private CyclomaticComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CyclomaticComplexityCalculator();
    }

    @Test
    void emptyCodeStillReturnsOne() {
        assertThat(calculator.calculate("")).isEqualTo(1);
    }

    @Test
    void singleIfIncrementsComplexity() {
        String code = "if (a > b) { return a; }";
        assertThat(calculator.calculate(code)).isEqualTo(2);
    }

    @Test
    void loopsIncrementComplexity() {
        String code = """
                for (int i = 0; i < n; i++) {
                    while (true) {
                        break;
                    }
                }
                """;
        assertThat(calculator.calculate(code)).isEqualTo(3);
    }

    @Test
    void switchCasesCount() {
        String code = """
                switch(value) {
                    case 1 -> {}
                    case 2 -> {}
                }
                """;
        assertThat(calculator.calculate(code)).isEqualTo(3);
    }

    @Test
    void logicalOperatorsCount() {
        String code = "if (a && b || c) { return 1; }";
        assertThat(calculator.calculate(code)).isEqualTo(4);
    }

    @Test
    void ternaryCounts() {
        String code = "int x = condition ? 1 : 2;";
        assertThat(calculator.calculate(code)).isEqualTo(2);
    }

    @Test
    void catchCountsAsBranch() {
        String code = """
                try {
                    risky();
                } catch (Exception ex) {
                    handle();
                }
                """;
        assertThat(calculator.calculate(code)).isEqualTo(2);
    }

    @Test
    void ignoresKeywordsInsideComments() {
        String code = """
                // if inside comment
                /*
                    while loop comment
                */
                int x = 0;
                """;
        assertThat(calculator.calculate(code)).isEqualTo(1);
    }

    @Test
    void ignoresKeywordsInsideStrings() {
        String code = "String text = \"if for while\";";
        assertThat(calculator.calculate(code)).isEqualTo(1);
    }

    @Test
    void complexSnippetAggregatesAllCounts() {
        String code = """
                public int compute(int a, int b) {
                    if (a > b && b > 0) {
                        return a;
                    } else if (a == b || b == 0) {
                        return b;
                    }
                    return a > b ? a : b;
                }
                """;
        assertThat(calculator.calculate(code)).isEqualTo(6);
    }
}
