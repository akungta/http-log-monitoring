package com.akashrungta.service;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.model.SummaryEvent;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

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

    public void summarize(){
        Instant now = Instant.now();
        Instant minusTenSeconds = now.minusSeconds(10);
        List<HttpEvent> httpEventsToBeSummarized = Lists.newArrayList();
        while(httpEventsQueue.peek() != null && !httpEventsQueue.peek().getInstant().isAfter(now)){
            HttpEvent event = httpEventsQueue.poll();
            if(event.getInstant().isBefore(minusTenSeconds)){
                // unprocessed/out-of-order http event before this time-period
                continue;
            } else {
                httpEventsToBeSummarized.add(httpEventsQueue.poll());
            }
        }
        SummaryEvent summaryEvent = new SummaryEvent();
        for(HttpEvent httpEvent : httpEventsToBeSummarized){
            summaryEvent.incrementSectionHits(httpEvent.getSection());
            summaryEvent.incrementStatusCode(httpEvent.getStatusCode());
            summaryEvent.addUserId(httpEvent.getUserId());
            summaryEvent.addBytes(httpEvent.getBytes());
        }
        eventBus.post(summaryEvent);
    }

}
