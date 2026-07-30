package com.pulseboard.service;

import com.pulseboard.model.CheckResult;
import com.pulseboard.model.CheckStatus;
import com.pulseboard.model.Endpoint;
import com.pulseboard.model.Incident;
import com.pulseboard.model.IncidentStatus;
import com.pulseboard.observer.AlertObserver;
import com.pulseboard.repository.CheckResultRepository;
import com.pulseboard.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Decides when a run of failed checks becomes a declared incident, and when a
 * recovery closes it back out. This is the line between "a check failed" and
 * "an incident is open": a single DOWN check is just a data point; two DOWN
 * checks in a row is treated as a real outage worth alerting on.
 *
 * Spring injects every bean implementing AlertObserver into the `observers` list
 * automatically (Observer Pattern) - this class never has to know how many
 * observers exist or what they do.
 */
@Service
public class IncidentService {

    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final List<AlertObserver> observers;

    public IncidentService(CheckResultRepository checkResultRepository,
                            IncidentRepository incidentRepository,
                            List<AlertObserver> observers) {
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
        this.observers = observers;
    }

    /**
     * Called once per check, right after the CheckResult is saved.
     */
    public void evaluate(Endpoint endpoint, CheckResult latestResult) {
        Incident openIncident = incidentRepository.findFirstByEndpoint_IdAndStatus(endpoint.getId(), IncidentStatus.OPEN);

        if (latestResult.getStatus() == CheckStatus.DOWN) {
            if (openIncident == null && isTwoConsecutiveDown(endpoint)) {
                openIncident(endpoint);
            }
            // If an incident is already open, do nothing - this is what stops an
            // alert storm. One incident produces exactly one "opened" notification,
            // no matter how many more 60-second checks fail while it's still down.
        } else {
            if (openIncident != null) {
                resolveIncident(endpoint, openIncident);
            }
        }
    }

    private boolean isTwoConsecutiveDown(Endpoint endpoint) {
        List<CheckResult> lastTwo = checkResultRepository.findTop2ByEndpoint_IdOrderByCheckedAtDesc(endpoint.getId());
        return lastTwo.size() == 2 && lastTwo.stream().allMatch(r -> r.getStatus() == CheckStatus.DOWN);
    }

    private void openIncident(Endpoint endpoint) {
        Incident incident = new Incident(endpoint, LocalDateTime.now());
        Incident saved = incidentRepository.save(incident);
        observers.forEach(o -> o.onIncidentOpened(endpoint, saved));
    }

    private void resolveIncident(Endpoint endpoint, Incident incident) {
        incident.setResolvedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.RESOLVED);
        Incident saved = incidentRepository.save(incident);
        observers.forEach(o -> o.onIncidentResolved(endpoint, saved));
    }
}
