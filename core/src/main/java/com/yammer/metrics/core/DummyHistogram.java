package com.yammer.metrics.core;

import com.yammer.metrics.stats.Snapshot;

/**
 * Histogram implementation that does nothing
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 1:15 PM
 */
public class DummyHistogram extends Histogram {
    public static final Histogram INSTANCE = new DummyHistogram();

    DummyHistogram() {
        super(SampleType.UNIFORM);
    }

    @Override
    public void update(long value) {
        // nothing
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
    public double min() {
        return 0.0;
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        // nothing
    }

    @Override
    public double stdDev() {
        return 0.0;
    }

    @Override
    public double sum() {
        return 0.0;
    }

    @Override
    public void update(int value) {
        // nothing
    }
}
