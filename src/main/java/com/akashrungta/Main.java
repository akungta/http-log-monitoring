package com.akashrungta;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;

public class Main {

    private static final long SLEEP = 500;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }

    private void run() throws InterruptedException {
        MyListener listener = new MyListener();
        Tailer tailer = Tailer.create(new File("/home/akungta/test_access.log"), listener, SLEEP);
        while (true) {
            Thread.sleep(SLEEP);
        }
    }

    public class MyListener extends TailerListenerAdapter {

        @Override
        public void handle(String line) {
            System.out.println(line);
        }

    }
}