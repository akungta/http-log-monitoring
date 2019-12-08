package com.akashrungta;

import com.akashrungta.service.PrintConsoleService;
import com.akashrungta.listener.LogTailerListener;
import com.akashrungta.service.AlertService;
import com.akashrungta.service.SummaryService;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.Tailer;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final long SLEEP = 500;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Exiting..");
            }
        }));

        Main main = new Main();

        main.run();
    }

    private void run() throws InterruptedException {

        // setup eventbus to publish alerts
        EventBus eventBus = new EventBus();

        SummaryService summaryService = new SummaryService(eventBus);
        AlertService alertService = new AlertService(eventBus);
        PrintConsoleService printConsoleService = new PrintConsoleService();

        eventBus.register(printConsoleService);
        eventBus.register(summaryService);
        eventBus.register(alertService);

        // setup log listener, this will write to post to the eventbus
        LogTailerListener listener = new LogTailerListener(eventBus);
        Tailer tailer = Tailer.create(new File("/home/akungta/test_access.log"), listener, SLEEP);

        // every 10s process
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new ScheduledRunnable(summaryService), 10, 10, TimeUnit.SECONDS);

        // alert listener (some pub-sub thingy)

        while (true) {
            Thread.sleep(SLEEP);
        }
    }

    @RequiredArgsConstructor
    static class ScheduledRunnable implements Runnable {

        private final SummaryService summaryService;

        @Override
        public void run() {
            summaryService.summarize();
        }
    }

}