package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.Constants;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.metadata.format.FormatType;
import org.junit.After;
import org.junit.Test;

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

    @After
    public void tearDown() throws Exception {
        this.formatterCache.shutdown();
    }

    @Test
    public void testGet() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 1, 5000);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        String result = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);
        assertEquals("result", result);
        StoreInfoAndData info = persistentStore.get(key);
        assertEquals(changeDate, info.getChangeDate());
        assertEquals(false, info.isPublished());
        assertEquals("result", info.getDataAsString());
        StoreInfo basicInfo = persistentStore.getInfo(key);
        assertEquals(changeDate, basicInfo.getChangeDate());
        assertEquals(false, basicInfo.isPublished());

        result = formatterCache.get(key, new ChangeDateValidator(changeDate), new Callable<StoreInfoAndData>() {
            @Override
            public StoreInfoAndData call() throws Exception {
                throw new AssertionError("Should not be called because cache should be up-to-date");
            }
        }, true);
        assertEquals("result", result);

        final long updatedChangeDate = changeDate + 100;
        result = formatterCache.get(key, new ChangeDateValidator(updatedChangeDate), new TestLoader("newVal", updatedChangeDate, false), true);
        assertEquals("newVal", result);
        info = persistentStore.get(key);
        assertEquals(updatedChangeDate, info.getChangeDate());
        assertEquals(false, info.isPublished());
        assertEquals("newVal", info.getDataAsString());
        basicInfo = persistentStore.getInfo(key);
        assertEquals(updatedChangeDate, basicInfo.getChangeDate());
        assertEquals(false, basicInfo.isPublished());
    }

    @Test
    public void testGetPublic() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100, 5000);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);

        assertNull(formatterCache.getPublished(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 100), new TestLoader("published", changeDate, true), true);
        assertArrayEquals("published".getBytes(Constants.CHARSET), formatterCache.getPublished(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 1000), new TestLoader("lastResult", changeDate, false), true);
        assertNull(formatterCache.getPublished(key));
    }

    @Test
    public void testMemoryCache() throws Exception {
        final AtomicBoolean persistentStoreHit = new AtomicBoolean(false);
        this.formatterCache = new FormatterCache(new PersistentStore() {
            @Override
            public StoreInfoAndData get(Key key) {
                persistentStoreHit.set(true);
                return new StoreInfoAndData("lksjdf", 234982734, false);
            }

            @Override
            public StoreInfo getInfo(Key key) {
                return get(key);
            }

            @Override
            public void put(Key key, StoreInfoAndData data) {
                // ignore
            }

            @Nullable
            @Override
            public byte[] getPublished(Key key) {
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
        }, 100, 5000);


        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result",changeDate, false), true);
        assertEquals(true, persistentStoreHit.get());

        persistentStoreHit.set(false);
        String result = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result",changeDate, false), true);
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
            Runnable createPersistentStoreRunnable(BlockingQueue<Pair<Key, StoreInfoAndData>> storeRequests, PersistentStore store) {
                return new PersistentStoreRunnable(storeRequests, store) {
                    @Override
                    void doStore(Pair<Key, StoreInfoAndData> request) throws InterruptedException, IOException, SQLException {
                        waitForStartPut.set(true);
                        while(!waitForAllowPut.get()) {
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
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), false);
        waitForStartPut.set(true);
        assertNull(persistentStore.get(key));
        waitForAllowPut.set(true);
        while(!waitForDone.get()) {
            Thread.sleep(100);
        }
        assertNotNull(persistentStore.get(key));
    }

}