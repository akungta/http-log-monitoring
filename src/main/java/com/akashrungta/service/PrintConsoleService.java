package com.akashrungta.service;

import com.akashrungta.model.SummaryEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintConsoleService {

    @Subscribe
    public void subscribe(SummaryEvent summaryEvent){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n****SUMMARY****\n");
        sb.append("From [").append(summaryEvent.getFrom()).append("] To [").append(summaryEvent.getTo()).append("]\n");
        sb.append("Total Requests: ").append(summaryEvent.getTotalRequests()).append("\n");
        sb.append("Total Bytes: ").append(summaryEvent.getTotalBytes()).append("\n");
        sb.append("Total Unique Users: ").append(summaryEvent.getUnqiueUserIds().size()).append("\n");
        sb.append("Hits per Section:\n");
        summaryEvent.getHitsPerSections().forEach((section, hits) -> {
            sb.append("\t").append(section).append("=").append(hits).append("\n");
        });
        sb.append("HTTP StatusCodes Counts:\n");
        summaryEvent.getStatusCodeCounts().forEach((statusCode, count) -> {
            sb.append("\t").append(statusCode).append("=").append(count).append("\n");
        });
        sb.append("\n");
        System.out.println(sb.toString());
    }

}
