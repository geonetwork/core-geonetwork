package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.services.metadata.format.FormatType;
import org.junit.After;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
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
        this.formatterCache = new FormatterCache(persistentStore, 1);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        String result = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);
        assertEquals("result", result);
        StoreInfoAndData info = persistentStore.get(key);
        assertEquals(changeDate, info.getChangeDate());
        assertEquals(false, info.isPublished());
        assertEquals("result", info.getResult());
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
        assertEquals("newVal", info.getResult());
        basicInfo = persistentStore.getInfo(key);
        assertEquals(updatedChangeDate, basicInfo.getChangeDate());
        assertEquals(false, basicInfo.isPublished());
    }

    @Test
    public void testGetPublic() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100);

        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), true);

        assertNull(formatterCache.getPublic(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 100), new TestLoader("published", changeDate, true), true);
        assertEquals("published", formatterCache.getPublic(key));

        formatterCache.get(key, new ChangeDateValidator(changeDate + 1000), new TestLoader("lastResult", changeDate, false), true);
        assertNull(formatterCache.getPublic(key));
    }

    @Test
    public void testMemoryCache() throws Exception {
        final AtomicBoolean persistentStoreHit = new AtomicBoolean(false);
        this.formatterCache = new FormatterCache(new PersistentStore() {
            @Override
            public StoreInfoAndData get(Key key) {
                persistentStoreHit.set(true);
                return new StoreInfoAndData("lksjdf", 234982734, false, 98345, 0);
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
            public String getPublic(Key key) {
                throw new UnsupportedOperationException("to implement");
            }
        }, 100);


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

    @Test
    public void testThreadedPutWorks() throws Exception {
        final MemoryPersistentStore persistentStore = new MemoryPersistentStore();
        this.formatterCache = new FormatterCache(persistentStore, 100, new AbstractExecutorService() {
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


        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        final Key key = new Key(1, "eng", FormatType.html, "full_view", hideWithheld);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, false), false);
    }

    private static class TestLoader implements Callable<StoreInfoAndData> {
        private final String resultToStore;
        private final long changeDate;
        private final boolean published;

        public TestLoader(String resultToStore, long changeDate, boolean published) {
            this.resultToStore = resultToStore;
            this.changeDate = changeDate;
            this.published = published;
        }

        @Override
        public StoreInfoAndData call() throws Exception {
            return new StoreInfoAndData(resultToStore, changeDate, published, 98345, 0);
        }
    }
}