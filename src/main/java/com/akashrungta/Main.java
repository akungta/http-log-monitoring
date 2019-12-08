package com.akashrungta;

import com.akashrungta.listener.ConsoleListener;
import com.akashrungta.listener.LogTailerListener;
import com.google.common.eventbus.EventBus;
import org.apache.commons.io.input.Tailer;

import java.io.File;

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
        eventBus.register(new ConsoleListener());

        // setup log listener, this will write to shared data store
        LogTailerListener listener = new LogTailerListener(eventBus);
        Tailer tailer = Tailer.create(new File("/home/akungta/test_access.log"), listener, SLEEP);
        while (true) {
            Thread.sleep(SLEEP);
        }

        // every 10s process which will read from the shared data store

        // alert listner (some pub-sub thingy)

    }

}