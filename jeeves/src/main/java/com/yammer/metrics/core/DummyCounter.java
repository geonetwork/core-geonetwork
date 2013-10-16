package com.yammer.metrics.core;

/**
 * A Counter that ignores input
 * User: jeichar
 * Date: 4/3/12
 * Time: 11:55 AM
 */
public class DummyCounter extends Counter {
    public static final Counter INSTANCE = new DummyCounter();

    DummyCounter() {
        super();
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
    public void dec() {
        // nothing
    }

    @Override
    public void dec(long n) {
        // nothing
    }

    @Override
    public void inc() {
        // nothing
    }

    @Override
    public void inc(long n) {
        // nothing
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        // nothing
    }
}
