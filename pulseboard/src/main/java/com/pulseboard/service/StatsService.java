package com.pulseboard.service;

import com.pulseboard.dto.StatsResponse;
import com.pulseboard.model.CheckResult;
import com.pulseboard.model.CheckStatus;
import com.pulseboard.repository.CheckResultRepository;
import com.pulseboard.util.PercentileCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * p50/p95 response time + uptime% over a rolling window, computed from the raw
 * check_results history.
 *
 * This is done in application memory rather than in SQL: MySQL (pre-8.0 window
 * function niceties aside) has no native PERCENTILE_CONT, so an exact percentile
 * either means an approximation via NTILE/ORDER BY+LIMIT tricks, or pulling the
 * window's rows back and sorting in Java - which is what we do here. Fine at
 * personal-project scale (thousands of rows per endpoint per week); if this had
 * to scale to millions of checks, you'd pre-aggregate into hourly rollups instead
 * of scanning raw rows on every request.
 */
@Service
public class StatsService {

    private final CheckResultRepository checkResultRepository;

    public StatsService(CheckResultRepository checkResultRepository) {
        this.checkResultRepository = checkResultRepository;
    }

    public StatsResponse getStats(Long endpointId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<CheckResult> results = checkResultRepository
                .findByEndpoint_IdAndCheckedAtAfterOrderByCheckedAtAsc(endpointId, since);

        if (results.isEmpty()) {
            return new StatsResponse(endpointId, hours, 0, 0.0, null, null);
        }

        List<Long> responseTimes = results.stream()
                .map(CheckResult::getResponseTimeMs)
                .sorted()
                .toList();

        long upCount = results.stream().filter(r -> r.getStatus() == CheckStatus.UP).count();
        double uptimePercentage = (upCount * 100.0) / results.size();

        return new StatsResponse(
                endpointId,
                hours,
                results.size(),
                Math.round(uptimePercentage * 100.0) / 100.0,
                PercentileCalculator.percentile(responseTimes, 50),
                PercentileCalculator.percentile(responseTimes, 95)
        );
    }
}
