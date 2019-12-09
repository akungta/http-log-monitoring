package com.akashrungta.service;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.model.SummaryEvent;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class SummaryService {

    private final ConcurrentLinkedQueue<HttpEvent> httpEventsQueue;

    private final EventBus eventBus;

    public SummaryService(EventBus eventBus){
        this.eventBus = eventBus;
        this.httpEventsQueue = new ConcurrentLinkedQueue<>();
    }

    @Subscribe
    public void subscribe(HttpEvent event){
        this.httpEventsQueue.add(event);
    }

    public void summarize(int summaryInterval){
        Instant now = Instant.now();
        Instant minusInterval = now.minusSeconds(summaryInterval);
        List<HttpEvent> httpEventsToBeSummarized = Lists.newArrayList();
        while(httpEventsQueue.peek() != null && !httpEventsQueue.peek().getInstant().isAfter(now)){
            HttpEvent event = httpEventsQueue.poll();
            // ignore unprocessed/out-of-order http event before the interval
            if(!event.getInstant().isBefore(minusInterval)){
                httpEventsToBeSummarized.add(event);
            }
        }
        log.debug("http events to summarize size " + httpEventsToBeSummarized.size());
        if(!httpEventsToBeSummarized.isEmpty()) {
            SummaryEvent summaryEvent = new SummaryEvent(minusInterval, now);
            for (HttpEvent httpEvent : httpEventsToBeSummarized) {
                summaryEvent.incrementRequestCount();
                summaryEvent.incrementSectionHits(httpEvent.getSection());
                summaryEvent.incrementStatusCode(httpEvent.getStatusCode());
                summaryEvent.addUserId(httpEvent.getUserId());
                summaryEvent.addBytes(httpEvent.getBytes());
            }
            log.debug("created summary " + summaryEvent);
            eventBus.post(summaryEvent);
        }
    }

}
