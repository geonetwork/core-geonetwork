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

import com.google.common.collect.Sets;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.domain.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FormatterCacheTest {

    private FormatterCache formatterCache;

    @Before
    public void setUp() throws Exception {
        ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
        ApplicationContextHolder.set(applicationContext);
        Mockito.when(applicationContext.getBean(SystemInfo.class)).thenReturn(SystemInfo.createForTesting(SystemInfo.STAGE_PRODUCTION));
    }

    @After
    public void tearDown() throws Exception {
        this.formatterCache.shutdown();
    }

    @Test
    public void testClear() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100, 5000);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();

        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        final Key key2 = new Key(2, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);

        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);
        formatterCache.get(key2, new ChangeDateValidator(changeDate), new TestLoader("result1", changeDate, false), true);

        formatterCache.clear();

        assertArrayEquals("newVal1".getBytes(Constants.CHARSET), formatterCache.get(key, new ChangeDateValidator(changeDate),
            new TestLoader("newVal1", changeDate, false), true));
        assertArrayEquals("newVal2".getBytes(Constants.CHARSET), formatterCache.get(key2, new ChangeDateValidator(changeDate),
            new TestLoader("newVal2", changeDate, false), true));
    }

    @Test
    public void testTypesToCache() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        final ConfigurableCacheConfig config = new ConfigurableCacheConfig();
        config.setAllowedTypes(Sets.newHashSet(FormatType.xml));
        this.formatterCache = new FormatterCache(persistentStore, 1, 5000, config);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        assertEquals("result", getAsString(key, changeDate, new TestLoader("result", changeDate, false)));
        assertEquals("new result", getAsString(key, changeDate, new TestLoader("new result", changeDate, false)));
    }

    @Test
    public void testGet() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 1, 5000);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        final TestLoader loader = new TestLoader("result", changeDate, false);
        String result = getAsString(key, changeDate, loader);
        assertEquals("result", result);
        StoreInfoAndData info = persistentStore.get(key);
        assertEquals(changeDate, info.getChangeDate());
        assertEquals(false, info.isPublished());
        assertEquals("result", info.getDataAsString());
        StoreInfo basicInfo = persistentStore.getInfo(key);
        assertEquals(changeDate, basicInfo.getChangeDate());
        assertEquals(false, basicInfo.isPublished());

        result = getAsString(key, changeDate, new Callable<StoreInfoAndDataLoadResult>() {
            @Override
            public StoreInfoAndDataLoadResult call() throws Exception {
                throw new AssertionError("Should not be called because cache should be up-to-date");
            }
        });
        assertEquals("result", result);

        final long updatedChangeDate = changeDate + 600;
        result = getAsString(key, updatedChangeDate, new TestLoader("newVal", updatedChangeDate, false));
        assertEquals("newVal", result);
        info = persistentStore.get(key);
        assertEquals(updatedChangeDate, info.getChangeDate());
        assertEquals(false, info.isPublished());
        assertEquals("newVal", info.getDataAsString());
        basicInfo = persistentStore.getInfo(key);
        assertEquals(updatedChangeDate, basicInfo.getChangeDate());
        assertEquals(false, basicInfo.isPublished());
    }

    private String getAsString(Key key, long changeDate, Callable<StoreInfoAndDataLoadResult> loader) throws Exception {
        byte[] bytes = formatterCache.get(key, new ChangeDateValidator(changeDate), loader, true);
        return new String(bytes, Constants.CHARSET);
    }

    @Test
    public void testGetPublic() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100, 5000);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);

        assertNull(formatterCache.getPublished(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 600), new TestLoader("published", changeDate, true), true);
        assertArrayEquals("published".getBytes(Constants.CHARSET), formatterCache.getPublished(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 1000), new TestLoader("lastResult", changeDate, false), true);
        assertNull(formatterCache.getPublished(key));
    }

    @Test
    public void testGetPublicCachePopulatedWithNonWithheld() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100, 5000);

        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", false, FormatterWidth._100);
        final Key key2 = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);

        formatterCache.get(key, new ChangeDateValidator(changeDate), new Callable<StoreInfoAndDataLoadResult>() {
            @Override
            public StoreInfoAndDataLoadResult call() throws Exception {
                return new StoreInfoAndDataLoadResult("result", changeDate, true, key2, new TestLoader("result", changeDate, true));
            }
        }, true);

        assertNull(formatterCache.getPublished(key));
        assertNotNull(formatterCache.getPublished(key2));
    }

    @Test
    public void testMemoryCache() throws Exception {
        final AtomicBoolean persistentStoreHit = new AtomicBoolean(false);
        this.formatterCache = new FormatterCache(new PersistentStore() {
            @Override
            @Nullable
            public StoreInfoAndData get(@Nonnull Key key) {
                persistentStoreHit.set(true);
                return new StoreInfoAndData("lksjdf", 234982734, false);
            }

            @Override
            @Nullable
            public StoreInfo getInfo(@Nonnull Key key) {
                return get(key);
            }

            @Override
            public void put(@Nonnull Key key, @Nonnull StoreInfoAndData data) {
                // ignore
            }

            @Nullable
            @Override
            public byte[] getPublished(@Nonnull Key key) {
                throw new UnsupportedOperationException("to implement");
            }

            @Override
            public void remove(@Nonnull Key key) throws IOException, SQLException {
                // ignore
            }

            @Override
            public void setPublished(int metadataId, boolean published) {
                throw new UnsupportedOperationException("not yet implemented");
            }

            @Override
            public void clear() {
                // ignore
            }
        }, 100, 5000);


        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);
        assertEquals(true, persistentStoreHit.get());

        persistentStoreHit.set(false);
        byte[] bytes = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);
        String result = new String(bytes, Constants.CHARSET);
        assertEquals("result", result);
        assertEquals(false, persistentStoreHit.get());
    }

    @Test(timeout = 1000L)
    public void testThreadedPutWorks() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        final AtomicBoolean waitForStartPut = new AtomicBoolean(false);
        final AtomicBoolean waitForAllowPut = new AtomicBoolean(false);
        final AtomicBoolean waitForDone = new AtomicBoolean(false);
        this.formatterCache = new FormatterCache(persistentStore, 100, 5000) {
            @Override
            PersistentStoreRunnable createPersistentStoreRunnable(BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests,
                                                                  PersistentStore store) {
                return new PersistentStoreRunnable(storeRequests, store) {
                    @Override
                    void doStore(Pair<Key, StoreInfoAndDataLoadResult> request) throws Exception {
                        waitForStartPut.set(true);
                        while (!waitForAllowPut.get()) {
                            Thread.sleep(100);
                        }
                        super.doStore(request);
                        waitForDone.set(true);
                    }
                };
            }
        };

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), false);
        waitForStartPut.set(true);
        assertNull(persistentStore.get(key));
        waitForAllowPut.set(true);
        while (!waitForDone.get()) {
            Thread.sleep(100);
        }
        assertNotNull(persistentStore.get(key));
    }

}
