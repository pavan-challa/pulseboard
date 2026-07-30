package com.pulseboard.observer;

import com.pulseboard.model.Endpoint;
import com.pulseboard.model.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Always-on observer so incidents are visible in the app logs even if email
 * alerting hasn't been configured yet. Registered alongside EmailAlertObserver -
 * this is the "notify all registered alert handlers" part of the spec in action.
 */
@Component
public class LoggingAlertObserver implements AlertObserver {

    private static final Logger log = LoggerFactory.getLogger(LoggingAlertObserver.class);

    @Override
    public void onIncidentOpened(Endpoint endpoint, Incident incident) {
        log.warn(">>> INCIDENT OPENED: '{}' ({}) has failed 2+ consecutive checks. Incident #{} opened at {}.",
                endpoint.getName(), endpoint.getUrl(), incident.getId(), incident.getOpenedAt());
    }

    @Override
    public void onIncidentResolved(Endpoint endpoint, Incident incident) {
        log.info(">>> INCIDENT RESOLVED: '{}' ({}) is back up. Incident #{} resolved at {}.",
                endpoint.getName(), endpoint.getUrl(), incident.getId(), incident.getResolvedAt());
    }
}
