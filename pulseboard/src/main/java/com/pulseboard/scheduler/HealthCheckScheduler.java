package com.pulseboard.scheduler;

import com.pulseboard.model.Endpoint;
import com.pulseboard.repository.EndpointRepository;
import com.pulseboard.service.CheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fires every 60 seconds and pings every registered endpoint.
 *
 * Each endpoint check is wrapped in its own try/catch so one bad endpoint
 * (bad URL, DNS failure, thrown exception) can't kill the run for the rest -
 * and can't kill the scheduler thread itself, which would silently stop all
 * future monitoring until the app is restarted. That's the answer to
 * "what happens if the monitoring app's scheduler thread dies" - it doesn't,
 * because no single check is allowed to propagate an exception out of this method.
 */
@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final EndpointRepository endpointRepository;
    private final CheckService checkService;

    public HealthCheckScheduler(EndpointRepository endpointRepository, CheckService checkService) {
        this.endpointRepository = endpointRepository;
        this.checkService = checkService;
    }

    @Scheduled(fixedRate = 60_000)
    public void runChecks() {
        List<Endpoint> endpoints = endpointRepository.findAll();

        if (endpoints.isEmpty()) {
            log.debug("No endpoints registered yet - skipping this cycle.");
            return;
        }

        for (Endpoint endpoint : endpoints) {
            try {
                checkService.performCheck(endpoint);
            } catch (Exception e) {
                log.error("Unexpected error checking endpoint '{}' ({}): {}",
                        endpoint.getName(), endpoint.getUrl(), e.getMessage());
            }
        }
    }
}
