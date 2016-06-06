package org.fao.geonet.wro4j;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;

import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DiskbackedCacheTest {
    @Before
    public void setUp() throws Exception {
        Context.set(Context.standaloneContext());
    }

    @Test
    public void testPut() throws Exception {
        CacheKey key = new CacheKey("groupName", ResourceType.CSS);
        CacheValue value = CacheValue.valueOf("rawContent", "hash");

        final URL classFile = DiskbackedCacheTest.class.getResource(DiskbackedCache.class.getSimpleName() + ".class");
        final Path path = Paths.get(classFile.toURI()).getParent().resolve("db");
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path.getParent(), "cache*.db")) {
            for (Path path1 : paths) {
                Files.deleteIfExists(path1);
            }
        }

        try (DiskbackedCache cache = new DiskbackedCache(100, path.toString())) {
            cache.put(key, value);
            CacheValue loaded = cache.get(key);
            assertEquals(value.getHash(), loaded.getHash());
            assertEquals(value.getRawContent(), loaded.getRawContent());
            assertArrayEquals(value.getGzippedContent(), loaded.getGzippedContent());
        }

        try (DiskbackedCache cache2 = new DiskbackedCache(100, path.toString())) {
            CacheValue loaded = cache2.get(key);
            assertEquals(value.getHash(), loaded.getHash());
            assertEquals(value.getRawContent(), loaded.getRawContent());
            assertArrayEquals(value.getGzippedContent(), loaded.getGzippedContent());
        }
    }


    @Test
    public void testClear() throws Exception {
        CacheKey key = new CacheKey("groupName", ResourceType.CSS);
        CacheValue value = CacheValue.valueOf("rawContent", "hash");

        try (DiskbackedCache cache = new DiskbackedCache(100)) {
            cache.put(key, value);
            CacheValue loaded = cache.get(key);
            assertEquals(value.getHash(), loaded.getHash());
            assertEquals(value.getRawContent(), loaded.getRawContent());
            assertArrayEquals(value.getGzippedContent(), loaded.getGzippedContent());
        }
    }

    @Test(expected = ro.isdc.wro.WroRuntimeException.class)
    public void testDestroy() throws Exception {
        CacheKey key = new CacheKey("groupName", ResourceType.CSS);
        CacheValue value = CacheValue.valueOf("rawContent", "hash");
        DiskbackedCache cache = new DiskbackedCache(100);
        cache.put(key, value);
        cache.destroy();
        cache.get(key);
    }
}
