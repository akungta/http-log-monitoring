package com.akashrungta.service;

import com.akashrungta.model.AlertRecoveredEvent;
import com.akashrungta.model.AlertStartedEvent;
import com.akashrungta.model.HttpEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AlertService {

    private final EventBus eventBus;

    private final ConcurrentSkipListMap<Instant, Integer> requestsCounts;

    private final AtomicBoolean isAlerting;

    public AlertService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.requestsCounts = new ConcurrentSkipListMap<>(Comparator.comparingLong(Instant::getEpochSecond));
        this.isAlerting = new AtomicBoolean(false);
    }

    @Subscribe
    public void subscribe(HttpEvent event) {
        requestsCounts.merge(event.getInstant(), 1, Integer::sum);
    }

    public void checkAlerts(int alertThresholdRPS, int alertDuration) {
        Instant now = Instant.now();
        Instant minusDuration = now.minusSeconds(alertDuration);
        log.debug("Checking the alerts at {}", now);
        // fetch all the requests counts for the given duration
        ConcurrentNavigableMap<Instant, Integer> subMap = requestsCounts.tailMap(minusDuration);
        // sum of all the request within the given duration
        int totalSum = subMap.values().stream().mapToInt(Integer::intValue).sum();
        // check if the average of the requests is greater than the alert threshold
        if (totalSum / alertDuration >= alertThresholdRPS) {
            log.debug("threshold of the alerts is reached with {}", totalSum);
            // set the flag to alerting, and check the previous state
            boolean wasAlerting = isAlerting.getAndSet(true);
            // if previous state was not alerting, start alerting now
            if (!wasAlerting) {
                log.debug("Alerting");
                eventBus.post(new AlertStartedEvent(now, totalSum));
            }
        } else {
            // set the flag to non-alerting, and check the previous state
            boolean wasAlerting = isAlerting.getAndSet(false);
            // if previous state was not alerting, send recovery alert
            if (wasAlerting) {
                log.debug("Alerting Recovered");
                eventBus.post(new AlertRecoveredEvent(now));
            }
        }
    }

    public void clearAlerts(int alertDuration) {
        log.debug("Running the cleanup");
        // clear all the element older than double the interval plus buffer of 10 seconds
        requestsCounts.headMap(Instant.now().minusSeconds(alertDuration).minusSeconds(10)).clear();
    }

}
