package com.pulseboard.dto;

import com.pulseboard.model.CheckStatus;

import java.time.LocalDateTime;

public class EndpointResponse {

    private Long id;
    private String name;
    private String url;
    private Integer checkIntervalSeconds;
    private LocalDateTime createdAt;
    private CheckStatus currentStatus;
    private Long lastResponseTimeMs;
    private LocalDateTime lastCheckedAt;

    public EndpointResponse(Long id, String name, String url, Integer checkIntervalSeconds,
                             LocalDateTime createdAt, CheckStatus currentStatus,
                             Long lastResponseTimeMs, LocalDateTime lastCheckedAt) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.createdAt = createdAt;
        this.currentStatus = currentStatus;
        this.lastResponseTimeMs = lastResponseTimeMs;
        this.lastCheckedAt = lastCheckedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Integer getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CheckStatus getCurrentStatus() {
        return currentStatus;
    }

    public Long getLastResponseTimeMs() {
        return lastResponseTimeMs;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }
}
