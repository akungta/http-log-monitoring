package com.akashrungta.model;

import lombok.Value;

import java.time.Instant;

@Value
public class AlertStartedEvent {
    Instant instant;
    long totalRequests;
}
