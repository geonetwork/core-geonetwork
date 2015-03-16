package org.fao.geonet;

import org.fao.geonet.util.ThreadPool;

import java.util.concurrent.TimeUnit;

/**
 * @author Jesse on 3/16/2015.
 */
public class TestThreadPool extends ThreadPool {
    @Override
    public void runTask(Runnable task, int delayBeforeStart, TimeUnit unit) {
        task.run();
    }
}
