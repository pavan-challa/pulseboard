package com.pulseboard.strategy;

import com.pulseboard.model.CheckStatus;
import com.pulseboard.model.Endpoint;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Phase 1 check strategy: plain HTTP GET.
 * UP  = response received with a 2xx or 3xx status code, within the timeout.
 * DOWN = non-2xx/3xx status, timeout, connection refused, DNS failure, etc.
 */
@Component
public class HttpGetCheckStrategy implements HealthCheckStrategy {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public CheckOutcome check(Endpoint endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint.getUrl()))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        long start = System.currentTimeMillis();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            long elapsed = System.currentTimeMillis() - start;

            CheckStatus status = (response.statusCode() >= 200 && response.statusCode() < 400)
                    ? CheckStatus.UP
                    : CheckStatus.DOWN;

            return new CheckOutcome(status, elapsed);
        } catch (Exception e) {
            // Timeout, connection refused, unknown host, SSL failure, etc all count as DOWN.
            long elapsed = System.currentTimeMillis() - start;
            return new CheckOutcome(CheckStatus.DOWN, elapsed);
        }
    }
}
