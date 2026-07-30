package com.pulseboard.observer;

import com.pulseboard.model.Endpoint;
import com.pulseboard.model.Incident;

/**
 * Observer Pattern: anything that needs to react when an incident opens or resolves
 * implements this and registers itself as a Spring bean. IncidentService doesn't know
 * or care how many observers exist or what they do with the notification - it just
 * calls every one of them. Today that's a log line and (optionally) an email; a Slack
 * webhook or SMS handler later is a new class, not a change to IncidentService.
 */
public interface AlertObserver {

    void onIncidentOpened(Endpoint endpoint, Incident incident);

    void onIncidentResolved(Endpoint endpoint, Incident incident);
}
