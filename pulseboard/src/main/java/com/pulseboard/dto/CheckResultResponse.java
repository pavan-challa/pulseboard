package com.pulseboard.dto;

import com.pulseboard.model.CheckResult;
import com.pulseboard.model.CheckStatus;

import java.time.LocalDateTime;

public class CheckResultResponse {

    private final Long id;
    private final CheckStatus status;
    private final Long responseTimeMs;
    private final LocalDateTime checkedAt;

    public CheckResultResponse(CheckResult r) {
        this.id = r.getId();
        this.status = r.getStatus();
        this.responseTimeMs = r.getResponseTimeMs();
        this.checkedAt = r.getCheckedAt();
    }

    public Long getId() {
        return id;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
}
