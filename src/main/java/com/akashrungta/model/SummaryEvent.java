package com.akashrungta.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
@RequiredArgsConstructor
public class SummaryEvent {
    private final Instant from;
    private final Instant to;
    private final Map<String, Integer> hitsPerSections = Maps.newHashMap();
    private final Map<Integer, Integer> statusCodeCounts = Maps.newHashMap();
    private final Set<String> uniqueUserIds = Sets.newHashSet();
    private int totalRequests = 0;
    private int totalBytes = 0;

    public void incrementRequestCount() {
        totalRequests++;
    }

    public void incrementSectionHits(String section) {
        hitsPerSections.merge(section, 1, Integer::sum);
    }

    public void incrementStatusCode(Integer statusCode) {
        statusCodeCounts.merge(statusCode, 1, Integer::sum);
    }

    public void addUserId(String userId) {
        if (userId != null) {
            uniqueUserIds.add(userId);
        }
    }

    public void addBytes(int bytes) {
        totalBytes += bytes;
    }
}
