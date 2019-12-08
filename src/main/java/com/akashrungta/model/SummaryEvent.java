package com.akashrungta.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Getter
public class SummaryEvent {
    private Map<String, Integer> hitsPerSections = Maps.newHashMap();
    private Map<Integer, Integer> statusCodeCounts = Maps.newHashMap();
    private Set<String> unqiueUserIds = Sets.newHashSet();
    private int totalBytes = 0;

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
