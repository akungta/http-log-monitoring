package com.akashrungta.service;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.model.SummaryEvent;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class SummaryService {

    private final EventBus eventBus;

    private final ConcurrentLinkedQueue<HttpEvent> httpEventsQueue;

    public SummaryService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.httpEventsQueue = new ConcurrentLinkedQueue<>();
    }

    @Subscribe
    public void subscribe(HttpEvent event) {
        httpEventsQueue.add(event);
    }

    public void summarize(int summaryInterval) {
        Instant now = Instant.now();
        Instant minusInterval = now.minusSeconds(summaryInterval);
        List<HttpEvent> httpEventsToBeSummarized = Lists.newArrayList();
        // loop to collect all the events in the queue till the current
        while (httpEventsQueue.peek() != null && !httpEventsQueue.peek().getInstant().isAfter(now)) {
            HttpEvent event = httpEventsQueue.poll();
            // ignore unprocessed/out-of-order http event before the interval
            if (event != null && !event.getInstant().isBefore(minusInterval)) {
                httpEventsToBeSummarized.add(event);
            }
        }
        log.debug("Http events to summarize size {}", httpEventsToBeSummarized.size());
        if (!httpEventsToBeSummarized.isEmpty()) {
            SummaryEvent summaryEvent = new SummaryEvent(minusInterval, now);
            for (HttpEvent httpEvent : httpEventsToBeSummarized) {
                summaryEvent.incrementRequestCount();
                summaryEvent.incrementSectionHits(httpEvent.getSection());
                summaryEvent.incrementStatusCode(httpEvent.getStatusCode());
                summaryEvent.addUserId(httpEvent.getUserId());
                summaryEvent.addBytes(httpEvent.getBytes());
            }
            log.debug("Created summary {}", summaryEvent);
            eventBus.post(summaryEvent);
        }
    }

}
