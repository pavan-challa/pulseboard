package com.pulseboard.service;

import com.pulseboard.model.CheckResult;
import com.pulseboard.model.Endpoint;
import com.pulseboard.repository.CheckResultRepository;
import com.pulseboard.strategy.CheckOutcome;
import com.pulseboard.strategy.HealthCheckStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service layer: runs a single endpoint through its check strategy and persists the result.
 * Kept separate from the scheduler so the scheduler stays a thin trigger and this logic
 * is unit-testable / reusable (e.g. later from a manual "check now" API).
 */
@Service
public class CheckService {

    private static final Logger log = LoggerFactory.getLogger(CheckService.class);

    private final HealthCheckStrategy checkStrategy;
    private final CheckResultRepository checkResultRepository;
    private final IncidentService incidentService;

    public CheckService(HealthCheckStrategy checkStrategy, CheckResultRepository checkResultRepository,
                         IncidentService incidentService) {
        this.checkStrategy = checkStrategy;
        this.checkResultRepository = checkResultRepository;
        this.incidentService = incidentService;
    }

    public CheckResult performCheck(Endpoint endpoint) {
        CheckOutcome outcome = checkStrategy.check(endpoint);

        CheckResult result = new CheckResult(endpoint, outcome.getStatus(), outcome.getResponseTimeMs());
        CheckResult saved = checkResultRepository.save(result);

        log.info("[{}] {} -> {} ({} ms)", endpoint.getName(), endpoint.getUrl(),
                outcome.getStatus(), outcome.getResponseTimeMs());

        // Phase 2: incident detection (2+ consecutive DOWN => open incident) and
        // Observer-pattern alert notification (opened / resolved) happen here.
        incidentService.evaluate(endpoint, saved);

        return saved;
    }
}
