package com.pulseboard.observer;

import com.pulseboard.model.Endpoint;
import com.pulseboard.model.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Sends the actual email alerts. Only registered as a bean when spring.mail.host is set,
 * so the app still starts cleanly (falling back to log-only alerts via LoggingAlertObserver)
 * before you've configured SMTP credentials.
 *
 * Only fires on the OPEN -> DOWN transition and the OPEN -> RESOLVED transition (see
 * IncidentService), never on every failed check - that's what prevents an alert storm:
 * one incident, at most two emails (opened + resolved), no matter how long it stays down
 * or how many 60-second checks run while it's down.
 */
@Component
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class EmailAlertObserver implements AlertObserver {

    private static final Logger log = LoggerFactory.getLogger(EmailAlertObserver.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender mailSender;

    @Value("${app.alert.recipient-email}")
    private String recipientEmail;

    @Value("${spring.mail.username:pulseboard@localhost}")
    private String fromAddress;

    public EmailAlertObserver(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void onIncidentOpened(Endpoint endpoint, Incident incident) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("[PulseBoard] DOWN: " + endpoint.getName());
        message.setText(
                "PulseBoard detected an outage.\n\n" +
                "Service: " + endpoint.getName() + "\n" +
                "URL: " + endpoint.getUrl() + "\n" +
                "Opened at: " + incident.getOpenedAt().format(FORMAT) + "\n\n" +
                "This alert fires once per incident - you won't get another email until it recovers."
        );
        send(message, endpoint);
    }

    @Override
    public void onIncidentResolved(Endpoint endpoint, Incident incident) {
        Duration downtime = Duration.between(incident.getOpenedAt(), incident.getResolvedAt());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("[PulseBoard] RESOLVED: " + endpoint.getName());
        message.setText(
                "PulseBoard confirms the service has recovered.\n\n" +
                "Service: " + endpoint.getName() + "\n" +
                "URL: " + endpoint.getUrl() + "\n" +
                "Opened at: " + incident.getOpenedAt().format(FORMAT) + "\n" +
                "Resolved at: " + incident.getResolvedAt().format(FORMAT) + "\n" +
                "Total downtime: " + downtime.toMinutes() + " minute(s)"
        );
        send(message, endpoint);
    }

    private void send(SimpleMailMessage message, Endpoint endpoint) {
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Never let a failed email send crash the scheduler thread - just log it.
            log.error("Failed to send alert email for endpoint '{}': {}", endpoint.getName(), e.getMessage());
        }
    }
}
