package com.yammer.metrics.core;

import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Performs no action.
 * User: jeichar
 * Date: 4/3/12
 * Time: 12:02 PM
 */
public class DummyTimer extends Timer {
    private static final TimeUnit TU = TimeUnit.MILLISECONDS;
    public static final Timer INSTANCE = new DummyTimer();
    DummyTimer() {
        super(DummyExecutorService.INSTANCE, TU, TU);
    }

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public long count() {
        return 0L;
    }

    @Override
    public TimeUnit durationUnit() {
        return TU;
    }

    @Override
    public String eventType() {
        return "";    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public double fifteenMinuteRate() {
        return 0.0;
    }

    @Override
    public double fiveMinuteRate() {
        return 0.0;
    }

    @Override
    public Snapshot getSnapshot() {
        return null;
    }

    @Override
    public double max() {
        return 0.0;
    }

    @Override
    public double mean() {
        return 0.0;
    }

    @Override
    public double meanRate() {
        return 0.0;
    }

    @Override
    public double min() {
        return 0.0;
    }

    @Override
    public double oneMinuteRate() {
        return 0.0;
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        // nothing
    }

    @Override
    public TimeUnit rateUnit() {
        return super.rateUnit();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public double stdDev() {
        return super.stdDev();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        super.stop();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public double sum() {
        return super.sum();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public TimerContext time() {
        return super.time();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return super.time(event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        super.update(duration, unit);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
