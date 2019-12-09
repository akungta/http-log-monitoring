package com.akashrungta.service;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.model.SummaryEvent;
import com.google.common.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Random;

import static org.junit.Assert.*;

public class SummaryServiceTest {

    public EventBus eventBus = Mockito.mock(EventBus.class);

    public SummaryService summaryService = new SummaryService(eventBus);

    @Test
    public void testSummarize() throws InterruptedException {
        // previous event to be ignored by summary
        summaryService.subscribe(fakeEvent("/a", 200));
        Thread.sleep(5000);
        int countof200 = 0;
        int countof500 = 0;
        int countofA = 0;
        int countofB = 0;
        Random r = new Random();
        for(int i=0; i < 5; i++){
            String section;
            if(r.nextBoolean()){
                section = "/a";
                countofA++;
            } else {
                section = "/b";
                countofB++;
            }
            int status;
            if(r.nextBoolean()){
                status = 200;
                countof200++;
            } else {
                status = 500;
                countof500++;
            }
            summaryService.subscribe(fakeEvent(section, status));
            Thread.sleep(500);
        }
        summaryService.summarize(5);
        ArgumentCaptor<SummaryEvent> arg = ArgumentCaptor.forClass(SummaryEvent.class);
        Mockito.verify(eventBus).post(arg.capture());
        SummaryEvent actual = arg.getValue();
        Assert.assertEquals(5, actual.getTotalRequests());
        Assert.assertEquals(5*10, actual.getTotalBytes());
        if(actual.getStatusCodeCounts().containsKey(200)) {
            Assert.assertEquals(countof200, (int) actual.getStatusCodeCounts().get(200));
        }
        if(actual.getStatusCodeCounts().containsKey(500)) {
            Assert.assertEquals(countof500, (int) actual.getStatusCodeCounts().get(500));
        }
        if(actual.getHitsPerSections().containsKey("/a")) {
            Assert.assertEquals(countofA, (int) actual.getHitsPerSections().get("/a"));
        }
        if(actual.getStatusCodeCounts().containsKey("/b")) {
            Assert.assertEquals(countofB, (int) actual.getHitsPerSections().get("/b"));
        }
    }

    private HttpEvent fakeEvent(String section, int statusCode){
        HttpEvent.HttpEventBuilder builder = HttpEvent.builder();
        builder.ip("127.0.0.1");
        builder.instant(Instant.now());
        builder.bytes(10);
        builder.section(section);
        builder.statusCode(statusCode);
        return builder.build();
    }

}