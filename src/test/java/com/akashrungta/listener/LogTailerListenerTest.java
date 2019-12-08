package com.akashrungta.listener;

import com.akashrungta.model.HttpEvent;
import com.google.common.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class LogTailerListenerTest {

    private EventBus eventBus = Mockito.mock(EventBus.class);
    private LogTailerListener logTailerListener = new LogTailerListener(eventBus);

}