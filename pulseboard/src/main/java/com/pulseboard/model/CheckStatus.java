package com.pulseboard.model;

/**
 * Result of a single health check. Stored as VARCHAR(10) in check_results.status.
 */
public enum CheckStatus {
    UP,
    DOWN
}
