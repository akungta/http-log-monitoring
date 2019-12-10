package com.akashrungta.service;

import com.akashrungta.model.AlertRecoveredEvent;
import com.akashrungta.model.AlertStartedEvent;
import com.akashrungta.model.HttpEvent;
import com.google.common.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AlertServiceTest {

    private static EventBus eventBus = mock(EventBus.class);

    private static AlertService alertService = new AlertService(eventBus);

    @Test
    public void testCheckAlerts() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            alertService.subscribe(fakeEvent(Instant.now()));
            Thread.sleep(200);
        }
        // this check is expected to create alert start
        alertService.checkAlerts(5, 2);
        Thread.sleep(2000);
        // this check is expected to recover alert
        alertService.checkAlerts(5, 2);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(eventBus, times(2)).post(captor.capture());
        List<Object> alerts = captor.getAllValues();
        // first event post is start of the event
        AlertStartedEvent alertStartedEvent = (AlertStartedEvent) alerts.get(0);
        Assert.assertNotNull(alertStartedEvent);
        Assert.assertEquals(10, alertStartedEvent.getTotalRequests());
        // second event post is recovery of the event
        AlertRecoveredEvent alertRecoveredEvent = (AlertRecoveredEvent) alerts.get(1);
        Assert.assertNotNull(alertRecoveredEvent);
        Assert.assertTrue(alertRecoveredEvent.getInstant().isAfter(alertStartedEvent.getInstant()));
    }

    private HttpEvent fakeEvent(Instant instant) {
        HttpEvent.HttpEventBuilder builder = HttpEvent.builder();
        builder.ip("127.0.0.1");
        builder.instant(instant);
        builder.bytes(10);
        builder.section("/a");
        builder.statusCode(200);
        return builder.build();
    }
}