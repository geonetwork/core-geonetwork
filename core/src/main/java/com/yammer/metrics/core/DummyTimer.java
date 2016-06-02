/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package com.yammer.metrics.core;

import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Performs no action. User: jeichar Date: 4/3/12 Time: 12:02 PM
 */
public class DummyTimer extends Timer {
    public static final Timer INSTANCE = new DummyTimer();
    private static final TimeUnit TU = TimeUnit.MILLISECONDS;

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
