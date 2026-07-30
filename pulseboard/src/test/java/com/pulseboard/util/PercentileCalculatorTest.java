package com.pulseboard.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentileCalculatorTest {

    @Test
    void emptyListReturnsZero() {
        assertEquals(0, PercentileCalculator.percentile(Collections.emptyList(), 50));
    }

    @Test
    void singleValueReturnsThatValueForAnyPercentile() {
        List<Long> values = List.of(120L);
        assertEquals(120L, PercentileCalculator.percentile(values, 50));
        assertEquals(120L, PercentileCalculator.percentile(values, 95));
    }

    @Test
    void p50OfTenEvenlySpacedValues() {
        // 10 values: 100..1000 step 100, already sorted ascending
        List<Long> values = Arrays.asList(100L, 200L, 300L, 400L, 500L, 600L, 700L, 800L, 900L, 1000L);
        // nearest-rank p50 of 10 values -> ceil(0.5 * 10) = 5th value (1-indexed) = 500
        assertEquals(500L, PercentileCalculator.percentile(values, 50));
    }

    @Test
    void p95OfTenEvenlySpacedValues() {
        List<Long> values = Arrays.asList(100L, 200L, 300L, 400L, 500L, 600L, 700L, 800L, 900L, 1000L);
        // ceil(0.95 * 10) = 10th value (1-indexed) = 1000
        assertEquals(1000L, PercentileCalculator.percentile(values, 95));
    }

    @Test
    void p95WithOneSlowOutlier() {
        List<Long> values = Arrays.asList(50L, 52L, 55L, 51L, 53L, 54L, 50L, 52L, 51L, 900L);
        Collections.sort(values);
        // outlier at the top should dominate p95 with only 10 samples
        assertEquals(900L, PercentileCalculator.percentile(values, 95));
    }
}
