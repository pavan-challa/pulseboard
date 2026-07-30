package com.pulseboard.repository;

import com.pulseboard.model.Incident;
import com.pulseboard.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Used to check whether an endpoint already has an open incident
    // (prevents opening duplicate incidents / sending duplicate alert storms)
    Incident findFirstByEndpoint_IdAndStatus(Long endpointId, IncidentStatus status);

    // Used by the public status page to show "yellow" for a bit after recovery,
    // rather than snapping straight back to green the instant it's back up.
    Incident findFirstByEndpoint_IdAndStatusAndResolvedAtAfter(Long endpointId, IncidentStatus status, LocalDateTime since);
}
