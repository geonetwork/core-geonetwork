package org.fao.geonet.services.metadata.format.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

/**
 * Caches Formatter html files in memory (keeping the most recent or most accessed X formatters) and on disk.
 * <p/>
 * The Formatter cache has two caches.
 * <ul>
 * <li>
 *      A fast access in-memory cache which is limited to some X records.
 * </li>
 * <li>
 *     A persistent cache which keeps a cache of every formatter that has been added to the cache.
 * </li>
 * </ul>
 * <p/>
 * When a value is added to the cache the value is added to the in-memory cache and to a queue for writing to the disk.
 * A separate thread is responsible for reading from the queue and writing all the values to the persistent cache.  This allows the
 * value to be written to the request in parallel with writing to the cache.
 * <p/>
 * Note: The Persistent cache used can be configured.
 *
 * @author Jesse on 3/5/2015.
 */
public class FormatterCache {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PersistentStore persistentStore;
    private final Cache<Key, StoreInfoAndData> memoryCache;
    private final ExecutorService executor;

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize) {
        this(persistentStore, memoryCacheSize, defaultExecutor());
    }

    private static ExecutorService defaultExecutor() {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setDaemon(true);
        threadFactory.setThreadNamePrefix("FormatterCache-");
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, ExecutorService executor) {
        this.persistentStore = persistentStore;
        this.memoryCache = CacheBuilder.<Key, StoreInfoAndData>newBuilder().
                maximumSize(memoryCacheSize).build();

        this.executor = executor;
    }

    @PreDestroy
    public void shutdown() {
        this.executor.shutdownNow();
    }



    /**
     * Get a value from the cache, or if it is not in the cache, load it with the loader and add it to the cache.
     *
     * @param key the lookup/store key
     * @param validator a strategy for checking if the value should be reloaded (for example if the metadata has changed since last
     *                  caching of the value)
     * @param loader the strategy to use for loading the value if the value is not in the cache (or is out-of-date).
     * @param writeToStoreInCurrentThread if true then the {@link org.fao.geonet.services.metadata.format.cache.PersistentStore} will
     *                                    be updated in the current thread instead of in another thread.
     */
    @Nullable
    public String get(Key key, Validator validator, Callable<StoreInfoAndData> loader, boolean writeToStoreInCurrentThread) throws
            Exception {
        StoreInfoAndData cached = memoryCache.getIfPresent(key);
        boolean invalid = false;
        if (cached != null && !validator.isCacheVersionValid(cached)) {
            cached = null;
            invalid = true;
        }

        if (!invalid && cached == null) {
            cached = loadFromPersistentCache(key, validator);
        }

        if (cached == null) {
            cached = loader.call();
            push(key, cached, writeToStoreInCurrentThread);

        }

        return cached.getResult();
    }

    private void push(Key key, StoreInfoAndData cached, boolean writeToStoreInCurrentThread) {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();

            this.memoryCache.put(key, cached);
            if (writeToStoreInCurrentThread) {
                this.persistentStore.put(key, cached);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private StoreInfoAndData loadFromPersistentCache(Key key, Validator validator) {
        final Lock readLock = lock.readLock();
        try {
            readLock.lock();
            final StoreInfo info = persistentStore.getInfo(key);
            if (info != null && validator.isCacheVersionValid(info)) {
                return persistentStore.get(key);
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }

    /**
     * Get a pre-cached public value.  This will very quickly get a public metadata if it has been pre-cached.  It is intended to
     * be a VERY fast lookup for search engine crawlers (for example).  If the metadata has not previously been cached it returns null.
     *<p/>
     * When a metadata is cached it is noted if it is public or not, if it the requested metadata public and cached then this method
     * will return it.  If it is not public or not cached it will not be returned.
     * <p/>
     * @param key the lookup key
     */
    @Nullable
    public String getPublic(Key key) {
        final Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return this.persistentStore.getPublic(key);
        } finally {
            readLock.unlock();
        }
    }


}
