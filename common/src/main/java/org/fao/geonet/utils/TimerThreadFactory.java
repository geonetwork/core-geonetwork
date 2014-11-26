package org.fao.geonet.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 * @author Jesse on 11/6/2014.
 */
public class TimerThreadFactory implements ThreadFactory {
    AtomicInteger numberOfThread = new AtomicInteger();
    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread thread = new Thread(r, "TimerThread-" + numberOfThread.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    }
}
