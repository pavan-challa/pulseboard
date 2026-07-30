package com.pulseboard.util;

import java.util.List;

/**
 * Nearest-rank percentile calculation, pulled out of StatsService so it's a plain,
 * dependency-free unit that's trivial to test without spinning up Spring or a database.
 */
public final class PercentileCalculator {

    private PercentileCalculator() {
    }

    /**
     * @param sortedValues values already sorted ascending
     * @param p            percentile to compute, 0-100
     */
    public static long percentile(List<Long> sortedValues, int p) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return 0;
        }
        int rank = (int) Math.ceil(p / 100.0 * sortedValues.size());
        int index = Math.max(0, Math.min(sortedValues.size(), rank) - 1);
        return sortedValues.get(index);
    }
}
