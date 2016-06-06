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

/**
 * A Counter that ignores input User: jeichar Date: 4/3/12 Time: 11:55 AM
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
