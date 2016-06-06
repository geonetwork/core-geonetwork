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

package org.fao.geonet.monitor.gauge;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author Jesse on 5/8/2015.
 */
public abstract class AbstractOSMxBeanGauge<T> implements MetricsFactory<Gauge<T>> {

    private final String name;

    public AbstractOSMxBeanGauge(String name) {
        this.name = name;
    }

    @Override
    public Gauge<T> create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newGauge(OperatingSystemMXBean.class, name, new Gauge<T>() {

            @Override
            public T value() {
                OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean mxBean = (com.sun.management.OperatingSystemMXBean)
                        operatingSystemMXBean;

                    return getValue(mxBean);
                }
                return getDefaultValue();
            }

        });
    }

    protected abstract T getDefaultValue();

    protected abstract T getValue(com.sun.management.OperatingSystemMXBean operatingSystemMXBean);
}
