package com.akashrungta.service;

import com.akashrungta.model.AlertRecoveredEvent;
import com.akashrungta.model.AlertStartedEvent;
import com.akashrungta.model.SummaryEvent;
import org.junit.Test;

import java.time.Instant;

// check if console service doesn't have unintended null-pointers exception
public class PrintConsoleServiceTest {

    private final PrintConsoleService service = new PrintConsoleService();

    @Test
    public void testSubscribeSummaryEvent() {
        service.subscribe(new SummaryEvent(Instant.now(), Instant.now()));
    }

    @Test
    public void testSubscribeAlert() {
        service.subscribe(new AlertStartedEvent(Instant.now(), 10));
    }

    @Test
    public void testSubscribeAlertRecovered() {
        service.subscribe(new AlertRecoveredEvent(Instant.now()));
    }
}