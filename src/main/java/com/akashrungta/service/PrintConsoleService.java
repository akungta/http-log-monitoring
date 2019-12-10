package com.akashrungta.service;

import com.akashrungta.model.AlertRecoveredEvent;
import com.akashrungta.model.AlertStartedEvent;
import com.akashrungta.model.SummaryEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
public class PrintConsoleService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MMM/dd, HH:mm:ss")
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault());

    private static String formatInstant(Instant instant) {
        return formatter.format(instant);
    }

    @Subscribe
    public void subscribe(SummaryEvent summaryEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append("****SUMMARY****\n");
        sb.append("From {")
                .append(formatInstant(summaryEvent.getFrom()))
                .append("} To {")
                .append(formatInstant(summaryEvent.getTo())).append("}\n");
        sb.append("Total Requests: ").append(summaryEvent.getTotalRequests()).append("\n");
        sb.append("Total Bytes: ").append(summaryEvent.getTotalBytes()).append("\n");
        sb.append("Total Unique Users: ").append(summaryEvent.getUniqueUserIds().size()).append("\n");
        sb.append("Hits per Section:\n");
        summaryEvent.getHitsPerSections().forEach((section, hits) ->
                sb.append("  ").append(section).append("=").append(hits).append("\n"));
        sb.append("HTTP StatusCodes Counts:\n");
        summaryEvent.getStatusCodeCounts().forEach((statusCode, count) ->
                sb.append("  ").append(statusCode).append("=").append(count).append("\n"));
        sb.append("\n");
        System.out.println(sb.toString());
    }

    @Subscribe
    public void subscribe(AlertStartedEvent alertStartedEvent) {
        System.out.println(String.format("High traffic generated an alert - hits = %d, triggered at %s",
                alertStartedEvent.getTotalRequests(), formatInstant(alertStartedEvent.getInstant())));
    }

    @Subscribe
    public void subscribe(AlertRecoveredEvent alertRecoveredEvent) {
        System.out.println(String.format("Recovered at %s", formatInstant(alertRecoveredEvent.getInstant())));
    }

}
