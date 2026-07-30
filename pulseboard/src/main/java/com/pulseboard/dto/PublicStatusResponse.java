package com.pulseboard.dto;

import java.time.LocalDateTime;

/**
 * Deliberately minimal - the public status page shows a name and a traffic light,
 * nothing about internal URLs, response times, or infrastructure details.
 */
public class PublicStatusResponse {

    private final Long id;
    private final String name;
    private final String status; // GREEN, YELLOW, RED, UNKNOWN
    private final LocalDateTime lastCheckedAt;

    public PublicStatusResponse(Long id, String name, String status, LocalDateTime lastCheckedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.lastCheckedAt = lastCheckedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }
}
