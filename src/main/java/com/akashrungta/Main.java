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
    private int alertThresholdRPS = 10;

    @CommandLine.Option(names = {"-d", "--alertDuration"}, paramLabel = "NUM",
            description = "The duration window (seconds) to analyse the alerts. (Default: 120)")
    private int alertDuration = 120;

    @CommandLine.Option(names = {"-i", "--summaryInterval"}, paramLabel = "NUM",
            description = "The periodic interval (seconds) to compute summary. (Default: 10)")
    private int summaryIntervalInSeconds = 10;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true,
            description = "Print version information and exit.")
    private boolean versionHelpRequested;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    public void run() {

        validateArgs();

        File httpAccessLogFile = validateAndGetFile();

        System.out.println("Stating HTTP Log Monitoring\n\n");
        log.info("Starting");

        // setup eventbus to publish alerts
        EventBus eventBus = new EventBus();

        // service to create the summary of http access logs
        SummaryService summaryService = new SummaryService(eventBus);
        // service to alert in case of of high-traffic and recovery
        AlertService alertService = new AlertService(eventBus);
        // service to print the events in the console
        PrintConsoleService printConsoleService = new PrintConsoleService();

        eventBus.register(printConsoleService);
        eventBus.register(summaryService);
        eventBus.register(alertService);

        // executor service to schedule summary and alerts
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

        // scheduler to run the summary service every given interval, starting at the given interval
        scheduledExecutorService.scheduleAtFixedRate(new SummaryRunner(summaryService),
                summaryIntervalInSeconds, summaryIntervalInSeconds, TimeUnit.SECONDS);

        // scheduler to run the alert service every 1 second after the initial alert duration
        scheduledExecutorService.scheduleWithFixedDelay(new AlertRunner(alertService),
                alertDuration, 1, TimeUnit.SECONDS);

        // scheduler to cleanup the alerts events older than alertDuration
        scheduledExecutorService.scheduleWithFixedDelay(new AlertCleanupRunner(alertService),
                alertDuration, alertDuration, TimeUnit.SECONDS);

        // tail -f functionality to read the latest added entry to the log file, every 100ms
        Tailer tailer = new Tailer(httpAccessLogFile, new LogTailerListener(eventBus), 100, true);
        // start the reading of the log file
        tailer.run();

        // shutdown hook to clean up the eventbus, gracefully shutdown the tailer and scheduler threads
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

    private void validateArgs() {
        if (alertThresholdRPS == 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "alertThreshold cannot be 0");
        }
        if (alertDuration == 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "alertDuration cannot be 0");
        }
        if (summaryIntervalInSeconds == 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "summaryInterval cannot be 0");
        }
    }

    private File validateAndGetFile() {
        File retval = new File(file);
        if(!retval.isFile() || retval.isDirectory()){
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Problem with reading the http access log file %s.", file));
        }
        return retval;
    }

    @RequiredArgsConstructor
    private class SummaryRunner implements Runnable {

        private final SummaryService summaryService;

        @Override
        public void run() {
            try {
                summaryService.summarize(summaryIntervalInSeconds);
            } catch (Exception e) {
                log.error("exception while summarizing", e);
            }
        }
    }

    @RequiredArgsConstructor
    private class AlertRunner implements Runnable {

        private final AlertService alertService;

        @Override
        public void run() {
            try {
                alertService.checkAlerts(alertThresholdRPS, alertDuration);
            } catch (Exception e) {
                log.error("exception while calling alert trigger", e);
            }
        }
    }

    @RequiredArgsConstructor
    private class AlertCleanupRunner implements Runnable {

        private final AlertService alertService;

        @Override
        public void run() {
            try {
                alertService.clearAlerts(alertDuration);
            } catch (Exception e) {
                log.error("exception while cleaning up the alerts", e);
            }
        }
    }

    @RequiredArgsConstructor
    private static class LogTailerListener extends TailerListenerAdapter {

        private final EventBus eventBus;

        @Override
        public void handle(String logLine) {
            try {
                Optional<HttpEvent> httpEvent = LogUtils.parseLogLine(logLine);
                httpEvent.ifPresent(e -> eventBus.post(e));
            } catch (Exception e) {
                log.error("exception in log listener", e);
            }
        }

    }

}