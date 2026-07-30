package com.pulseboard.controller;

import com.pulseboard.dto.CheckResultResponse;
import com.pulseboard.dto.EndpointRequest;
import com.pulseboard.dto.EndpointResponse;
import com.pulseboard.dto.StatsResponse;
import com.pulseboard.service.EndpointService;
import com.pulseboard.service.StatsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
public class EndpointController {

    private final EndpointService endpointService;
    private final StatsService statsService;

    public EndpointController(EndpointService endpointService, StatsService statsService) {
        this.endpointService = endpointService;
        this.statsService = statsService;
    }

    // Register a new endpoint to monitor. The scheduler picks it up on its next 60s cycle.
    @PostMapping
    public ResponseEntity<EndpointResponse> register(@Valid @RequestBody EndpointRequest request) {
        EndpointResponse created = endpointService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // List all endpoints with their current (most recent) status - powers the dashboard grid.
    @GetMapping
    public List<EndpointResponse> listAll() {
        return endpointService.listAll();
    }

    // History for one endpoint. Defaults to last 24h; pass ?hours=168 for a 7-day window.
    @GetMapping("/{id}/checks")
    public List<CheckResultResponse> getChecks(@PathVariable Long id,
                                                @RequestParam(defaultValue = "24") int hours) {
        return endpointService.getRecentChecks(id, hours);
    }

    // p50/p95 response time + uptime% over the window. ?hours=168 for the 7-day view.
    @GetMapping("/{id}/stats")
    public StatsResponse getStats(@PathVariable Long id,
                                   @RequestParam(defaultValue = "24") int hours) {
        return statsService.getStats(id, hours);
    }
}
