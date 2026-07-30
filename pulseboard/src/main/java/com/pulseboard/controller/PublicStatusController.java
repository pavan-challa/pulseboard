package com.pulseboard.controller;

import com.pulseboard.dto.PublicStatusResponse;
import com.pulseboard.service.StatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Backs the public status page. Deliberately separate from EndpointController -
 * this is the "everyone can see this" surface, admin registration lives elsewhere.
 * (No auth is wired up in this project yet - see README "Known limitations".)
 */
@RestController
@RequestMapping("/api/public")
public class PublicStatusController {

    private final StatusService statusService;

    public PublicStatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public List<PublicStatusResponse> getStatus() {
        return statusService.getPublicStatus();
    }
}
