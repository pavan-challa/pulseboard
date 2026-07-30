package com.pulseboard.repository;

import com.pulseboard.model.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {
}
