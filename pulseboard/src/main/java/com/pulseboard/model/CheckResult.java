package com.pulseboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private CheckStatus status;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "checked_at", updatable = false)
    private LocalDateTime checkedAt;

    public CheckResult() {
    }

    public CheckResult(Endpoint endpoint, CheckStatus status, Long responseTimeMs) {
        this.endpoint = endpoint;
        this.status = status;
        this.responseTimeMs = responseTimeMs;
    }

    @PrePersist
    protected void onCreate() {
        this.checkedAt = LocalDateTime.now();
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

    public CheckStatus getStatus() {
        return status;
    }

    public void setStatus(CheckStatus status) {
        this.status = status;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
}
