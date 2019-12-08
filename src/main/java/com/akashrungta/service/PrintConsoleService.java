package com.akashrungta.service;

import com.akashrungta.model.SummaryEvent;
import com.google.common.eventbus.Subscribe;

public class PrintConsoleService {

    @Subscribe
    public void subscribe(SummaryEvent summaryEvent){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n****SUMMARY****\n\n");
        sb.append("HITS PER SECTION\n");
        summaryEvent.getHitsPerSections().forEach((section, hits) -> {
            sb.append(section).append(": ").append(hits).append("\n");
        });
        sb.append("\n");
        sb.append("COUNT OF UNIQUE USERS: ").append(summaryEvent.getUnqiueUserIds().size()).append("\n");
        sb.append("STATUS CODES COUNTS\n");
        summaryEvent.getStatusCodeCounts().forEach((statusCode, count) -> {
            sb.append(statusCode).append(": ").append(count).append("\n");
        });
        sb.append("\n");
    }

}
