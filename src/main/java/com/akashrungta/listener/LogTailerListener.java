package com.akashrungta.listener;

import com.akashrungta.service.SummaryService;
import com.akashrungta.model.HttpEvent;
import com.akashrungta.utils.LogUtils;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.Optional;

@RequiredArgsConstructor
public class LogTailerListener extends TailerListenerAdapter {

    private final EventBus eventBus;

    @Override
    public void handle(String logLine) {
        Optional<HttpEvent> httpEvent = LogUtils.parseLogLine(logLine);
        httpEvent.ifPresent(e -> eventBus.post(e));
    }

}
