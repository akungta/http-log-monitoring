package com.akashrungta;

import com.akashrungta.model.HttpEvent;
import com.akashrungta.service.PrintConsoleService;
import com.akashrungta.service.AlertService;
import com.akashrungta.service.SummaryService;
import com.akashrungta.utils.LogUtils;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import picocli.CommandLine;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@CommandLine.Command(version = "v1.0", header = "%nHTTP Log Monitoring Tool%n",
        description = "Prints usage help and version help when requested.%n")
public class Main implements Runnable {

    @CommandLine.Option(names = {"-f", "--file"}, paramLabel = "FILE",
            description = "The HTTP access log file. (Default: /tmp/access.log)")
    private String file = "/tmp/access.log";

    @CommandLine.Option(names = {"-t", "--alertThreshold"}, paramLabel = "NUM",
            description = "The threshold requests per seconds to trigger alerts. (Default: 10)")
    private int alertThresholdPerSeconds = 10;

    @CommandLine.Option(names = {"-i", "--summaryInterval"}, paramLabel = "NUM",
            description = "The periodic interval (seconds) to compute summary. (Default: 10)")
    private int summaryInterval = 10;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true,
            description = "Print version information and exit.")
    private boolean versionHelpRequested;

    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    public void run() {

        System.out.println("Stating HTTP Log Monitoring\n\n");
        log.info("Starting");

        // setup eventbus to publish alerts
        EventBus eventBus = new EventBus();

        SummaryService summaryService = new SummaryService(eventBus);
        AlertService alertService = new AlertService(eventBus);
        PrintConsoleService printConsoleService = new PrintConsoleService();

        eventBus.register(printConsoleService);
        eventBus.register(summaryService);
        eventBus.register(alertService);

        // every 10s process
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new ScheduledRunnable(summaryService, summaryInterval), summaryInterval, summaryInterval, TimeUnit.SECONDS);

        // alert listener (some pub-sub thingy)

        // setup log listener, this will post to the eventbus
        LogTailerListener listener = new LogTailerListener(eventBus);
        // tail -f functionality to read the latest added entry to the log file, every 100ms
        Tailer tailer = new Tailer(new File(file), listener, 100, true);
        tailer.run();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Exiting");
            log.info("Exiting");
            tailer.stop();
            eventBus.unregister(printConsoleService);
            eventBus.unregister(summaryService);
            eventBus.unregister(alertService);
            scheduledExecutorService.shutdown();
        }));
    }

    @RequiredArgsConstructor
    static class ScheduledRunnable implements Runnable {

        private final SummaryService summaryService;
        private final int summaryInterval;

        @Override
        public void run() {
            summaryService.summarize(summaryInterval);
        }
    }

    @RequiredArgsConstructor
    static class LogTailerListener extends TailerListenerAdapter {

        private final EventBus eventBus;

        @Override
        public void handle(String logLine) {
            log.debug("Received logline " + logLine);
            Optional<HttpEvent> httpEvent = LogUtils.parseLogLine(logLine);
            httpEvent.ifPresent(e -> eventBus.post(e));
        }

    }

}