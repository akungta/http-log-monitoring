package com.akashrungta.listener;

import com.google.common.eventbus.Subscribe;

public class ConsoleListener {

    @Subscribe
    public void printEvent(String log){
        System.out.println(log);
    }

}
