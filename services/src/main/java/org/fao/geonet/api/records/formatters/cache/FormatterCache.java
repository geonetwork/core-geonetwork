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

package org.fao.geonet.api.records.formatters.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.servlet.ServletContext;
import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Caches Formatter html files in memory (keeping the most recent or most accessed X formatters) and
 * on disk.
 * <p/>
 * The Formatter cache has two caches.
 * <ul>
 *     <li> A fast access in-memory cache which is limited to some X records.</li>
 *     <li> A persistent cache which keeps a cache of every formatter that has
 * been added to the cache. </li>
 * </ul>
 * <p/>
 * When a value is added to the cache the value is added to the in-memory cache and to a queue for
 * writing to the disk. A separate thread is responsible for reading from the queue and writing all
 * the values to the persistent cache. This allows the value to be written to the request in
 * parallel with writing to the cache.
 * <p/>
 * Note: The Persistent cache used can be configured.
 *
 * @author Jesse on 3/5/2015.
 */
public class FormatterCache {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PersistentStore persistentStore;
    private final Cache<Key, StoreInfoAndData> memoryCache;
    private final Multimap<Integer, Pair<Key, StoreInfoAndData>> mdIdIndex = ArrayListMultimap.create();
    private final ExecutorService executor;
    private final BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests;
    @Autowired
    private final CacheConfig cacheConfig;

    private final HashMap<String, String> cacheProperties = new HashMap<String, String>();
    private String landingPageFormatter;
    private Map<String, String> landingPageFormatterParameters = new HashMap<>();
    private String landingPageLanguage = Geonet.DEFAULT_LANGUAGE;
    private ServiceContext _context;

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, int maxStoreRequests) {
        this(persistentStore, memoryCacheSize, maxStoreRequests, new ConfigurableCacheConfig());
    }

    public FormatterCache(PersistentStore persistentStore, int memoryCacheSize, int maxStoreRequests,
                          CacheConfig cacheConfig, ExecutorService executor) {
        cacheProperties.put("MEMORY_MAX_SIZE", memoryCacheSize + "");
        cacheProperties.put("QUEUE_MAX_REQUEST_NUMBER", maxStoreRequests + "");

        this.persistentStore = persistentStore;
        this.memoryCache = CacheBuilder.newBuilder().
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
     * Get a value from the cache, or if it is not in the cache, load it with the loader and add it
     * to the cache.
     *
     * @param key                         the lookup/store key
     * @param validator                   a strategy for checking if the value should be reloaded
     *                                    (for example if the metadata has changed since last
     *                                    caching of the value)
     * @param loader                      the strategy to use for loading the value if the value is
     *                                    not in the cache (or is out-of-date).
     * @param writeToStoreInCurrentThread if true then the {@link org.fao.geonet.api.records.formatters.cache.PersistentStore}
     *                                    will be updated in the current thread instead of in
     *                                    another thread.
     */
    @Nullable
    public byte[] get(Key key, Validator validator, Callable<StoreInfoAndDataLoadResult> loader,
                      boolean writeToStoreInCurrentThread) throws Exception {
        final Lock readLock = lock.readLock();
        StoreInfoAndData cached;
        try {
            readLock.lock();
            if (!cacheConfig.allowCaching(key)) {
                return loader.call().data;
            }

            cached = memoryCache.getIfPresent(key);
            boolean invalid = false;
            if (cached != null && !validator.isCacheVersionValid(cached)) {
                cached = null;
                invalid = true;
            }

            if (!invalid && cached == null) {
                cached = loadFromPersistentCache(key, validator);
            }

        } finally {
            readLock.unlock();
        }
        final Lock writeLock = lock.writeLock();
        if (cached == null) {
            try {
                writeLock.lock();
                StoreInfoAndDataLoadResult loaded = loader.call();
                cached = loaded;
                push(key, loaded, writeToStoreInCurrentThread);
            } finally {
                writeLock.unlock();
            }
        }

        return cached.data;

    }

    private void push(Key key, StoreInfoAndDataLoadResult cached,
                      boolean writeToStoreInCurrentThread) throws IOException, SQLException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();

            this.memoryCache.put(key, cached);
            this.mdIdIndex.put(key.mdId, Pair.read(key, cached));
            if (writeToStoreInCurrentThread) {
                createPersistentStoreRunnable(storeRequests, persistentStore).processStoreRequest(Pair.read(key, cached));
                cacheProperties.put("FILESYSTEM_CACHE_SIZE", persistentStore.getSize() + "");

            } else {
                if (!this.executor.isShutdown()) {
                    this.storeRequests.put(Pair.read(key, cached));
                    cacheProperties.put("FILESYSTEM_CACHE_SIZE", persistentStore.getSize() + "");
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
     * Get a pre-cached public value.  This will very quickly get a public metadata if it has been
     * pre-cached.  It is intended to be a VERY fast lookup for search engine crawlers (for
     * example).  If the metadata has not previously been cached it returns null.
     * <p/>
     * When a metadata is cached it is noted if it is public or not, if it the requested metadata
     * public and cached then this method will return it.  If it is not public or not cached it will
     * not be returned.
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
     * @param metadataUuid the uuid of the metadata whose published state may have changed
     * @param published  mark all cached values for this metadata
     */
    void setPublished(int metadataId, String metadataUuid, boolean published) throws IOException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            this.persistentStore.setPublished(metadataId, metadataUuid, published);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove all cached values related to the metadataId.
     */
    public synchronized void removeAll(int metadataId) throws IOException, SQLException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            Collection<Pair<Key, StoreInfoAndData>> storeInfoAndDatas = this.mdIdIndex.removeAll(metadataId);
            for (Pair<Key, StoreInfoAndData> storeInfoAndData : storeInfoAndDatas) {
                final Key key = storeInfoAndData.one();
                this.memoryCache.invalidate(key);
                this.persistentStore.remove(key);
            }
            cacheProperties.put("FILESYSTEM_CACHE_SIZE", persistentStore.getSize() + "");
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clear all records from the cache and backing persistent cache.
     */
    public void clear() throws IOException, SQLException {
        final Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            this.memoryCache.invalidateAll();
            this.persistentStore.clear();
            cacheProperties.put("FILESYSTEM_CACHE_SIZE", persistentStore.getSize() + "");
        } finally {
            writeLock.unlock();
        }
    }

    public void setLandingPageFormatter(String landingPageFormatter) {
        if (landingPageFormatter.contains("?")) {
            final String[] strings = landingPageFormatter.split("\\?");
            this.landingPageFormatter = strings[0];
            if (strings.length > 1) {
                for (String param : strings[1].split("&")) {
                    if(param.contains("=")) {
                        final String[] paramAndValue = param.split("=");
                        this.landingPageFormatterParameters.put(paramAndValue[0], paramAndValue[1]);
                    } else {
                        this.landingPageFormatterParameters.put(param, "");
                    }
                }
            }
        } else {
            this.landingPageFormatter = landingPageFormatter;
        }
    }

    public void setContext(ServiceContext context) {
        this._context = context;
    }

    public void setLandingPageLanguage(String landingPageLanguage) {
        this.landingPageLanguage = landingPageLanguage;
    }

    private class RemoveFromIndexListener implements RemovalListener<Key, StoreInfoAndData> {
        @Override
        public void onRemoval(RemovalNotification<Key, StoreInfoAndData> notification) {
            mdIdIndex.remove(notification.getKey().mdId, notification.getValue());
        }
    }

    public Map<String, String> getInfo() {
        return cacheProperties;
    }

    /**
     * If one record available in the catalogue, init all formatters defined
     * in formattersToInitialize so they get loaded eg. XSLCache init
     * @param context
     */
    public static void initializeFormatters(final ServiceContext context) {
        final FormatterApi formatService = context.getBean(FormatterApi.class);

        Thread fillCaches = new Thread(new Runnable() {
            @Override
            public void run() {
                final ServletContext servletContext = context.getServlet().getServletContext();
                context.setAsThreadLocal();
                final Page<Metadata> metadatas = ApplicationContextHolder.get().getBean(MetadataRepository.class).findAll(PageRequest.of(0, 1));
                if (metadatas.getNumberOfElements() > 0) {
                    Integer mdId = metadatas.getContent().get(0).getId();
                    context.getUserSession().loginAs(new User().setName("admin").setProfile(Profile.Administrator).setUsername("admin"));
                    @SuppressWarnings("unchecked")
                    List<String> formattersToInitialize = ApplicationContextHolder.get().getBean("formattersToInitialize", List.class);

                    for (String formatterName : formattersToInitialize) {
                        Log.info(Geonet.GEONETWORK, "Initializing the Formatter with id: " + formatterName);
                        final MockHttpSession servletSession = new MockHttpSession(servletContext);
                        servletSession.setAttribute(Jeeves.Elem.SESSION,  context.getUserSession());
                        final MockHttpServletRequest servletRequest = new MockHttpServletRequest(servletContext);
                        servletRequest.setSession(servletSession);
                        final MockHttpServletResponse response = new MockHttpServletResponse();
                        try {
                            formatService.exec("eng", FormatType.html.toString(), mdId.toString(), null, formatterName,
                                Boolean.TRUE.toString(), false, FormatterWidth._100, new ServletWebRequest(servletRequest, response));
                        } catch (Throwable t) {
                            Log.info(Geonet.GEONETWORK, "Error while initializing the Formatter with id: " + formatterName, t);
                        }
                    }
                }
            }
        });
        fillCaches.setDaemon(true);
        fillCaches.setName("Fill formatter cache thread");
        fillCaches.setPriority(Thread.MIN_PRIORITY);
        fillCaches.start();
    }


    @Autowired
    OperationAllowedRepository operationAllowedRepo;
    @Autowired
    DataManager dataManager;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    FormatterApi formatService;

    /**
     * Fill the cache of landing pages.
     *
     * @param context
     */
    public void fillLandingPageCache(final ServiceContext context) {
        Thread fillCaches = new Thread(new Runnable() {
            @Override
            public void run() {
                final ServletContext servletContext = context.getServlet().getServletContext();
                context.setAsThreadLocal();

                final List<Integer> allPublicRecordIds = operationAllowedRepo.findAllPublicRecordIds();

                final MockHttpSession servletSession = new MockHttpSession(servletContext);
                servletSession.setAttribute(Jeeves.Elem.SESSION, context.getUserSession());
                final MockHttpServletRequest servletRequest = new MockHttpServletRequest(servletContext);
                servletRequest.setSession(servletSession);
                servletRequest.setParameters(landingPageFormatterParameters);
                final MockHttpServletResponse response = new MockHttpServletResponse();

                allPublicRecordIds.stream().forEach(r -> {
                    try {
                        formatService.getRecordFormattedBy(
                            MediaType.TEXT_HTML_VALUE,
                            landingPageFormatter,
                            metadataUtils.getMetadataUuid(r + ""),
                            FormatterWidth._100,
                            null,
                            landingPageLanguage,
                            FormatType.html,
                            true,
                            true,
                            false,
                            new ServletWebRequest(servletRequest, response),
                            servletRequest);
                    } catch (Throwable t) {
                        Log.info(Geonet.GEONETWORK, String.format(
                            "Error building the landing page with formatter '%s' for record '%s'.",
                            landingPageFormatter, r), t);
                    }
                });
            }
        });
        fillCaches.setDaemon(true);
        fillCaches.setName("Fill formatter cache thread");
        fillCaches.setPriority(Thread.MIN_PRIORITY);
        fillCaches.start();
    }

    /**
     * If a landing page formatter is defined, build the landing page
     * by calling the formatter service. Landing page are only computed
     * on public records (ie. no template, no private records).
     * @param metadataId
     */
    public void buildLandingPage(int metadataId) {
        if (_context == null) {
            return;
        }
        if (StringUtils.isNotEmpty(landingPageFormatter)) {
            final ServletContext servletContext = _context.getServlet().getServletContext();
            _context.setAsThreadLocal();
            final MockHttpSession servletSession = new MockHttpSession(servletContext);
            servletSession.setAttribute(Jeeves.Elem.SESSION, _context.getUserSession());
            final MockHttpServletRequest servletRequest = new MockHttpServletRequest(servletContext);
            servletRequest.setSession(servletSession);
            servletRequest.setParameters(landingPageFormatterParameters);
            final MockHttpServletResponse response = new MockHttpServletResponse();

            final OperationAllowed publicRecord = operationAllowedRepo.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(ReservedGroup.all.getId(), metadataId, ReservedOperation.view.getId());
            if (publicRecord != null) {
                final Metadata metadata = metadataRepository.findOneById(metadataId);
                if(metadata.getDataInfo().getType() == MetadataType.METADATA) {
                    try {
                        formatService.getRecordFormattedBy(
                            MediaType.TEXT_HTML_VALUE,
                            landingPageFormatter,
                            metadataUtils.getMetadataUuid(metadataId + ""),
                            FormatterWidth._100,
                            null,
                            landingPageLanguage,
                            FormatType.html,
                            true,
                            true,
                            true,
                            new ServletWebRequest(servletRequest, response),
                            servletRequest);
                    } catch (Throwable t) {
                        Log.info(Geonet.GEONETWORK, String.format(
                            "Error building the landing page with formatter '%s' for record '%s'.",
                            landingPageFormatter, metadataId), t);
                    }
                }
            }
        }
    }
}
