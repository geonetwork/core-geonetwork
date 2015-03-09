package org.fao.geonet.services.metadata.format.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.domain.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
 * A fast access in-memory cache which is limited to some X records.
 * </li>
 * <li>
 * A persistent cache which keeps a cache of every formatter that has been added to the cache.
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
    @Autowired
    private CacheConfig cacheConfig;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PersistentStore persistentStore;
    private final Cache<Key, StoreInfoAndData> memoryCache;
    private final Multimap<Integer, Pair<Key, StoreInfoAndData>> mdIdIndex = ArrayListMultimap.create();
    private final ExecutorService executor;
    private final BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests;

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, int maxStoreRequests) {
        this(persistentStore, memoryCacheSize, maxStoreRequests, new ConfigurableCacheConfig());
    }

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, int maxStoreRequests,
                          CacheConfig cacheConfig, ExecutorService executor) {
        this.persistentStore = persistentStore;
        this.memoryCache = CacheBuilder.<Key, StoreInfoAndData>newBuilder().
                removalListener(new RemoveFromIndexListener()).
                maximumSize(memoryCacheSize).build();
        this.cacheConfig = cacheConfig;
        this.storeRequests = new ArrayBlockingQueue<>(maxStoreRequests);

        this.executor = executor;
        this.executor.submit(createPersistentStoreRunnable(this.storeRequests, this.persistentStore));
    }

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, int maxStoreRequests, CacheConfig config) {
        this(persistentStore, memoryCacheSize, maxStoreRequests, config, defaultExecutor());
    }

    private static ExecutorService defaultExecutor() {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setDaemon(true);
        threadFactory.setThreadNamePrefix("FormatterCache-");
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    @VisibleForTesting
    PersistentStoreRunnable createPersistentStoreRunnable(BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests,
                                                          PersistentStore store) {
        return new PersistentStoreRunnable(storeRequests, store);
    }

    @PreDestroy
    public void shutdown() {
        this.executor.shutdownNow();
    }

    public void remove(Key key) throws IOException, SQLException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            this.memoryCache.invalidate(key);
            this.persistentStore.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get a value from the cache, or if it is not in the cache, load it with the loader and add it to the cache.
     *
     * @param key                         the lookup/store key
     * @param validator                   a strategy for checking if the value should be reloaded (for example if the metadata has
     *                                    changed since last
     *                                    caching of the value)
     * @param loader                      the strategy to use for loading the value if the value is not in the cache (or is out-of-date).
     * @param writeToStoreInCurrentThread if true then the {@link org.fao.geonet.services.metadata.format.cache.PersistentStore} will
     *                                    be updated in the current thread instead of in another thread.
     */
    @Nullable
    public byte[] get(Key key, Validator validator, Callable<StoreInfoAndDataLoadResult> loader,
                      boolean writeToStoreInCurrentThread) throws Exception {
        if (!cacheConfig.allowCaching(key)) {
            return loader.call().data;
        }

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
            StoreInfoAndDataLoadResult loaded = loader.call();
            cached = loaded;
            push(key, loaded, writeToStoreInCurrentThread);
        }

        return cached.data;
    }

    private void push(Key key, StoreInfoAndDataLoadResult cached,
                      boolean writeToStoreInCurrentThread) throws IOException, SQLException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();

            this.memoryCache.put(key, cached);
            this.mdIdIndex.put(key.mdId, Pair.read(key, (StoreInfoAndData) cached));
            if (writeToStoreInCurrentThread) {
                createPersistentStoreRunnable(storeRequests, persistentStore).processStoreRequest(Pair.read(key, cached));
            } else {
                if (!this.executor.isShutdown()) {
                    this.storeRequests.put(Pair.read(key, cached));
                }
            }
        } catch (InterruptedException e) {
            // return
        } finally {
            writeLock.unlock();
        }
    }

    private StoreInfoAndData loadFromPersistentCache(Key key, Validator validator) throws IOException, SQLException {
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
     * <p/>
     * When a metadata is cached it is noted if it is public or not, if it the requested metadata public and cached then this method
     * will return it.  If it is not public or not cached it will not be returned.
     * <p/>
     *
     * @param key the lookup key
     */
    @Nullable
    public byte[] getPublished(Key key) throws IOException, SQLException {
        final Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return this.persistentStore.getPublished(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Publish or unpublish all cached values related to the given metadata.
     *
     * @param metadataId the id of the metadata whose published state may have changed
     * @param published  mark all cached values for this metadata
     */
    synchronized void setPublished(int metadataId, boolean published) throws IOException {
        this.persistentStore.setPublished(metadataId, published);
    }

    public synchronized void removeAll(int metadataId) throws IOException, SQLException {
        Collection<Pair<Key, StoreInfoAndData>> storeInfoAndDatas = this.mdIdIndex.removeAll(metadataId);
        for (Pair<Key, StoreInfoAndData> storeInfoAndData : storeInfoAndDatas) {
            final Key key = storeInfoAndData.one();
            this.memoryCache.invalidate(key);
            this.persistentStore.remove(key);
        }
    }

    private class RemoveFromIndexListener implements RemovalListener<Key, StoreInfoAndData> {
        @Override
        public void onRemoval(RemovalNotification<Key, StoreInfoAndData> notification) {
            mdIdIndex.remove(notification.getKey().mdId, notification.getValue());
        }
    }
}
