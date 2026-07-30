package com.pulseboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a sustained outage for an endpoint (2+ consecutive failed checks).
 * Populated starting Phase 2 - the table exists now so the schema is stable end to end.
 */
@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private IncidentStatus status = IncidentStatus.OPEN;

    public Incident() {
    }

    public Incident(Endpoint endpoint, LocalDateTime openedAt) {
        this.endpoint = endpoint;
        this.openedAt = openedAt;
        this.status = IncidentStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
