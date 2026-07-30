package com.pulseboard.strategy;

import com.pulseboard.model.CheckStatus;

/**
 * Result of running a single health check strategy, before it's persisted as a CheckResult.
 */
public class CheckOutcome {

    private final CheckStatus status;
    private final long responseTimeMs;

    public CheckOutcome(CheckStatus status, long responseTimeMs) {
        this.status = status;
        this.responseTimeMs = responseTimeMs;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }
}
