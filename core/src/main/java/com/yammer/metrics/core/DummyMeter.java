package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * User: jeichar
 * Date: 4/3/12
 * Time: 1:18 PM
 */
public class DummyMeter extends Meter {
    public static final Meter INSTANCE = new DummyMeter();

    DummyMeter() {
        super(DummyExecutorService.INSTANCE, "Dumb", TimeUnit.MILLISECONDS, Clock.defaultClock());
    }
}
