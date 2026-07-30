package com.pulseboard.repository;

import com.pulseboard.model.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    // Last N hours of history for a given endpoint - powers the dashboard's 24h view
    List<CheckResult> findByEndpoint_IdAndCheckedAtAfterOrderByCheckedAtAsc(Long endpointId, LocalDateTime since);

    // Most recent check for an endpoint - powers the "current status" tile
    CheckResult findFirstByEndpoint_IdOrderByCheckedAtDesc(Long endpointId);

    // Last N results in order, used by the scheduler to test for consecutive failures (Phase 2)
    List<CheckResult> findTop2ByEndpoint_IdOrderByCheckedAtDesc(Long endpointId);
}
