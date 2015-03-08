package org.fao.geonet.services.metadata.format.cache;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * @author Jesse on 3/5/2015.
 */
public class NoCachingStore extends FormatterCache {
    public NoCachingStore() {
        super(new MemoryPersistentStore(), 10, 10, new ConfigurableCacheConfig(), new AbstractExecutorService() {
            @Override
            public void shutdown() {

            }

            @Override
            public List<Runnable> shutdownNow() {
                return null;
            }

            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            @Override
            public void execute(Runnable command) {

            }
        });
    }

    @Nullable
    @Override
    public byte[] get(Key key, Validator validator, Callable<StoreInfoAndDataLoadResult> loader,
                      boolean writeToStoreInCurrentThread) throws Exception {
        return loader.call().data;
    }

    @Nullable
    @Override
    public byte[] getPublished(Key key) {
        return null;
    }
}
