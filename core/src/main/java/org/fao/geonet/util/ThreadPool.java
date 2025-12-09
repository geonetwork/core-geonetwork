//==============================================================================
//===
//=== ThreadPool
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;

public class ThreadPool {
    public static final String SEQUENTIAL_EXECUTION = "geonetwork.sequential.execution";
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(20);
    int poolSize = 5;
    int maxPoolSize = 10;
    long keepAliveTime = 2;
    ThreadPoolExecutor threadPool = null;
    ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

    // --- threadpool will create all possible threads and queue tasks up to the
    // --- size of the queue - any tasks submitted after that will be run by
    // --- the caller thread (ie. the main thread) - this is why we create with
    // --- CallerRunsPolicy for rejected tasks
    public ThreadPool() {
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
            keepAliveTime, TimeUnit.SECONDS, queue,
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void runTask(Runnable task) {
        runTask(task, 0, TimeUnit.SECONDS);
    }

    public void runTask(Runnable task, int delayBeforeStart, TimeUnit unit) {
        if (Boolean.parseBoolean(System.getProperty(SEQUENTIAL_EXECUTION, "false"))) {
            task.run();
        } else {
            if (delayBeforeStart < 1) {
                if (Log.isDebugEnabled(Geonet.THREADPOOL)) {
                    Log.debug(Geonet.THREADPOOL, "Adding task to threadpool:" + toString());
                }
                threadPool.execute(task);
            } else {
                if (Log.isDebugEnabled(Geonet.THREADPOOL)) {
                    Log.debug(Geonet.THREADPOOL,
                        "Scheduling task to be executed in threadpool in " + delayBeforeStart + " " + unit + ": " + toString());
                }

                timer.schedule(new ScheduledTask(task), delayBeforeStart, unit);
            }
        }
    }

    @PreDestroy
    public void shutDown() {
        Log.info(Geonet.THREADPOOL, "Stopping the ThreadPool");
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.warning(Geonet.THREADPOOL, "Error while stopping threadPool", e);
        }
        timer.shutdown();
        try {
            timer.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.warning(Geonet.THREADPOOL, "Error while stopping threadPool", e);
        }
        Log.info(Geonet.THREADPOOL, "Stopped the ThreadPool");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("ThreadPool tasks | ");
        sb.append(" \t| total: ").append(threadPool.getTaskCount());
        sb.append(" \t| completed: ")
            .append(threadPool.getCompletedTaskCount());
        sb.append(" \t| active: ").append(threadPool.getActiveCount());
        sb.append(" \t| in queue: ").append(queue.size());
        sb.append(" \t| remaining in queue: ")
            .append(queue.remainingCapacity());

        return sb.toString();
    }

    private class ScheduledTask implements Runnable {

        private final Runnable task;

        public ScheduledTask(Runnable task) {
            this.task = task;
        }

        public void run() {
            threadPool.execute(task);
            if (Log.isDebugEnabled(Geonet.THREADPOOL)) {
                Log.debug(Geonet.THREADPOOL, "Adding task to threadpool after being scheduled: " + toString());
            }

        }

    }
}
