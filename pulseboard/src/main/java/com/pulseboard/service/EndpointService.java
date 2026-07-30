package com.pulseboard.service;

import com.pulseboard.dto.CheckResultResponse;
import com.pulseboard.dto.EndpointRequest;
import com.pulseboard.dto.EndpointResponse;
import com.pulseboard.model.CheckResult;
import com.pulseboard.model.Endpoint;
import com.pulseboard.repository.CheckResultRepository;
import com.pulseboard.repository.EndpointRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final CheckResultRepository checkResultRepository;

    public EndpointService(EndpointRepository endpointRepository, CheckResultRepository checkResultRepository) {
        this.endpointRepository = endpointRepository;
        this.checkResultRepository = checkResultRepository;
    }

    public EndpointResponse register(EndpointRequest request) {
        Endpoint endpoint = new Endpoint(request.getName(), request.getUrl());
        Endpoint saved = endpointRepository.save(endpoint);
        return toResponse(saved);
    }

    public List<EndpointResponse> listAll() {
        return endpointRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CheckResultResponse> getRecentChecks(Long endpointId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return checkResultRepository
                .findByEndpoint_IdAndCheckedAtAfterOrderByCheckedAtAsc(endpointId, since)
                .stream()
                .map(CheckResultResponse::new)
                .collect(Collectors.toList());
    }

    private EndpointResponse toResponse(Endpoint endpoint) {
        CheckResult latest = checkResultRepository.findFirstByEndpoint_IdOrderByCheckedAtDesc(endpoint.getId());
        return new EndpointResponse(
                endpoint.getId(),
                endpoint.getName(),
                endpoint.getUrl(),
                endpoint.getCheckIntervalSeconds(),
                endpoint.getCreatedAt(),
                latest != null ? latest.getStatus() : null,
                latest != null ? latest.getResponseTimeMs() : null,
                latest != null ? latest.getCheckedAt() : null
        );
    }
}
