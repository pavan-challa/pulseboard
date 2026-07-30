package com.pulseboard.service;

import com.pulseboard.dto.PublicStatusResponse;
import com.pulseboard.model.CheckResult;
import com.pulseboard.model.CheckStatus;
import com.pulseboard.model.Endpoint;
import com.pulseboard.model.Incident;
import com.pulseboard.model.IncidentStatus;
import com.pulseboard.repository.CheckResultRepository;
import com.pulseboard.repository.EndpointRepository;
import com.pulseboard.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes the green/yellow/red traffic light for the public status page.
 *
 * RED    - an incident is currently open (2+ consecutive failed checks)
 * YELLOW - no open incident, but either the very latest check failed (a single
 *          blip that hasn't crossed the 2-check threshold yet) or an incident
 *          resolved in roughly the last 24h - so a very recent recovery doesn't
 *          snap straight back to "all clear" the instant it's up
 * GREEN  - healthy, nothing notable recently
 * UNKNOWN - registered but no checks have run yet
 */
@Service
public class StatusService {

    private static final int RECENT_RECOVERY_WINDOW_HOURS = 24;

    private final EndpointRepository endpointRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;

    public StatusService(EndpointRepository endpointRepository,
                          CheckResultRepository checkResultRepository,
                          IncidentRepository incidentRepository) {
        this.endpointRepository = endpointRepository;
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
    }

    public List<PublicStatusResponse> getPublicStatus() {
        return endpointRepository.findAll().stream()
                .map(this::toStatus)
                .collect(Collectors.toList());
    }

    private PublicStatusResponse toStatus(Endpoint endpoint) {
        CheckResult latest = checkResultRepository.findFirstByEndpoint_IdOrderByCheckedAtDesc(endpoint.getId());

        if (latest == null) {
            return new PublicStatusResponse(endpoint.getId(), endpoint.getName(), "UNKNOWN", null);
        }

        Incident openIncident = incidentRepository.findFirstByEndpoint_IdAndStatus(endpoint.getId(), IncidentStatus.OPEN);
        if (openIncident != null) {
            return new PublicStatusResponse(endpoint.getId(), endpoint.getName(), "RED", latest.getCheckedAt());
        }

        Incident recentlyResolved = incidentRepository.findFirstByEndpoint_IdAndStatusAndResolvedAtAfter(
                endpoint.getId(), IncidentStatus.RESOLVED, LocalDateTime.now().minusHours(RECENT_RECOVERY_WINDOW_HOURS));

        boolean latestCheckFailed = latest.getStatus() == CheckStatus.DOWN;

        String status = (recentlyResolved != null || latestCheckFailed) ? "YELLOW" : "GREEN";
        return new PublicStatusResponse(endpoint.getId(), endpoint.getName(), status, latest.getCheckedAt());
    }
}
