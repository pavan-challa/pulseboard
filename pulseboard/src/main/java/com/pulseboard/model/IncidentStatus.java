package com.pulseboard.model;

/**
 * Lifecycle state of an incident. Stored as VARCHAR(20) in incidents.status.
 * Wired up in Phase 2 when incident detection is added.
 */
public enum IncidentStatus {
    OPEN,
    RESOLVED
}
