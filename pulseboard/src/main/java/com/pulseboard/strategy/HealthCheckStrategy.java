package com.pulseboard.strategy;

import com.pulseboard.model.Endpoint;

/**
 * Strategy Pattern: each implementation knows how to perform one *kind* of check
 * against an endpoint (plain HTTP GET, POST with an auth header, matching an
 * expected response body, etc). The scheduler doesn't need to know which kind
 * it's running - it just calls check(endpoint).
 *
 * Phase 1 ships {@link HttpGetCheckStrategy}. Later phases can add
 * AuthenticatedPostCheckStrategy / ExpectedBodyCheckStrategy without touching
 * the scheduler or the service layer.
 */
public interface HealthCheckStrategy {

    CheckOutcome check(Endpoint endpoint);
}
