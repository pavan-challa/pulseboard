package com.pulseboard.dto;

public class StatsResponse {

    private final Long endpointId;
    private final int windowHours;
    private final long sampleCount;
    private final double uptimePercentage;
    private final Long p50ResponseTimeMs;
    private final Long p95ResponseTimeMs;

    public StatsResponse(Long endpointId, int windowHours, long sampleCount, double uptimePercentage,
                          Long p50ResponseTimeMs, Long p95ResponseTimeMs) {
        this.endpointId = endpointId;
        this.windowHours = windowHours;
        this.sampleCount = sampleCount;
        this.uptimePercentage = uptimePercentage;
        this.p50ResponseTimeMs = p50ResponseTimeMs;
        this.p95ResponseTimeMs = p95ResponseTimeMs;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public int getWindowHours() {
        return windowHours;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public double getUptimePercentage() {
        return uptimePercentage;
    }

    public Long getP50ResponseTimeMs() {
        return p50ResponseTimeMs;
    }

    public Long getP95ResponseTimeMs() {
        return p95ResponseTimeMs;
    }
}
