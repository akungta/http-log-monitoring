package com.akashrungta.listener;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.utils.LogUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
@RequiredArgsConstructor
public class LogTailerListener extends TailerListenerAdapter {

    private final EventBus eventBus;

    @Override
    public void handle(String logLine) {
        Optional<HttpEvent> httpEvent = LogUtils.parseLogLine(logLine);
        httpEvent.ifPresent(e -> eventBus.post(e));
    }

}
