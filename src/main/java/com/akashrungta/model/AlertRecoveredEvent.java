package com.akashrungta.model;

import lombok.Value;

import java.time.Instant;

@Value
public class AlertRecoveredEvent {
    Instant instant;
}
