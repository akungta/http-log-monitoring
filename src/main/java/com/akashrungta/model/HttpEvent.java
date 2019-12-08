package com.akashrungta.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;

@Value
@Builder
public class HttpEvent {
    String ip;
    String hostname;
    Instant dateTime;
    String method;
    String section;
    String path;
    int statusCode;
}
