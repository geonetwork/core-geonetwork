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

package org.fao.geonet.domain;

import org.fao.geonet.utils.Log;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Just add a blocking stop method to be able to bloc until the threads are stopped.
 */
public class SpringScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public SpringScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @PreDestroy
    public void stop() {
        Log.info(Log.RESOURCES, "Stopping the ScheduledThreadPoolExecutor");
        shutdown();
        try {
            awaitTermination(60, TimeUnit.SECONDS);
            Log.info(Log.RESOURCES, "Stopped the ScheduledThreadPoolExecutor");
        } catch (InterruptedException e) {
            Log.warning(Log.RESOURCES, "Error stopping the ScheduledThreadPoolExecutor", e);
        }
    }
}
