package com.akashrungta.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
@RequiredArgsConstructor
public class SummaryEvent {
    private final Instant from;
    private final Instant to;
    private int totalRequests = 0;
    private Map<String, Integer> hitsPerSections = Maps.newHashMap();
    private Map<Integer, Integer> statusCodeCounts = Maps.newHashMap();
    private Set<String> unqiueUserIds = Sets.newHashSet();
    private int totalBytes = 0;

    public void incrementRequestCount(){
        totalRequests++;
    }

    public void incrementSectionHits(String section){
        hitsPerSections.merge(section, 1, Integer::sum);
    }

    public void incrementStatusCode(Integer statusCode){
        statusCodeCounts.merge(statusCode, 1, Integer::sum);
    }

    public void addUserId(String userId){
        if(userId != null){
            unqiueUserIds.add(userId);
        }
    }

    public void addBytes(int bytes){
        totalBytes+=bytes;
    }
}
