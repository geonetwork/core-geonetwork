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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.api.records.attachments.StoreFolderConfig;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FilesystemStoreTest {


    private FileSystem fileSystem;
    private FilesystemStore store;
    private GeonetworkDataDirectory geonetworkDataDirectory;

    @Before
    public void setUp() throws Exception {
        ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
        ApplicationContextHolder.set(applicationContext);
        Mockito.when(applicationContext.getBean(StoreFolderConfig.class)).thenReturn(new StoreFolderConfig());

        createDataDir();
        initStore();

    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
        store.close();
    }

    @Test
    public void testGet() throws Exception {
        StoreInfoAndData data = new StoreInfoAndData("result", 10000, false);
        final Key key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        final StoreInfoAndData loaded = store.get(key);

        assertEquals(data.getDataAsString(), loaded.getDataAsString());
        assertEquals(data.getChangeDate(), loaded.getChangeDate());
        assertEquals(data.isPublished(), loaded.isPublished());
        assertTrue(0 < countFiles(geonetworkDataDirectory.getHtmlCacheDir()));
    }

    @Test
    public void testGetInfo() throws Exception {
        StoreInfoAndData data = new StoreInfoAndData("result", 10000, false);
        final Key key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        final StoreInfo loaded = store.getInfo(key);

        assertFalse(loaded instanceof StoreInfoAndData);
        assertEquals(data.getChangeDate(), loaded.getChangeDate());
        assertEquals(data.isPublished(), loaded.isPublished());

    }

    @Test
    public void testGetPublic() throws Exception {

        StoreInfoAndData data = new StoreInfoAndData("result", 10000, false);
        Key key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        assertNull(store.getPublished(key));
        assertEquals(1, countFiles(store.getPrivatePath(key)));
        assertEquals(0, countFiles(store.getPublicPath(key)));

        data = new StoreInfoAndData("two", 10000, true);
        Key key2 = new Key(2, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key2, data);

        assertNotNull(store.getPublished(key2));
        assertNull(store.getPublished(key));
        assertEquals(3, countFiles(geonetworkDataDirectory.getHtmlCacheDir()));

        data = new StoreInfoAndData("three", 10000, true);
        store.put(key, data);
        assertNotNull(store.getPublished(key2));
        assertNotNull(store.getPublished(key));
        assertEquals(4, countFiles(geonetworkDataDirectory.getHtmlCacheDir()));

        data = new StoreInfoAndData("four", 10000, false);
        store.put(key2, data);
        assertNull(store.getPublished(key2));
        assertNotNull(store.getPublished(key));
        assertEquals(3, countFiles(geonetworkDataDirectory.getHtmlCacheDir()));
    }

    @Test
    public void testDoNotPublishMdWithWithheld() throws IOException, SQLException {

        StoreInfoAndData data = new StoreInfoAndData("result", 10000, true);
        Key key = new Key(1, "eng", FormatType.html, "full_view", false, FormatterWidth._100);
        store.put(key, data);
        assertNull(store.getPublished(key));

        store.setPublished(1, true);
        assertNull(store.getPublished(key));

        data = new StoreInfoAndData("result", 10000, true);
        key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        assertNotNull(store.getPublished(key));

        store.setPublished(1, true);
        assertNotNull(store.getPublished(key));
    }

    @Test
    public void testPublicPrivatePath() throws Exception {
        Key key = new Key(2, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        assertFalse(this.store.getPrivatePath(key).equals(this.store.getPublicPath(key)));
    }

    @Test
    public void testDiskSizeRestrictionReduceSizeOnOverflow() throws Exception {
        Key[] keys = prepareDiskSizeRestrictionTests();

        store.put(keys[5], new StoreInfoAndData(new byte[200], 6, false));
        assertStoreContains(keys, keys[3], keys[4], keys[5]);

    }


    @Test
    public void testDiskSizeRestrictionReplace() throws Exception {
        Key[] keys = prepareDiskSizeRestrictionTests();

        store.put(keys[4], new StoreInfoAndData(new byte[200], 4, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[0], new StoreInfoAndData(new byte[200], 0, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[1], new StoreInfoAndData(new byte[200], 1, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[3], new StoreInfoAndData(new byte[200], 3, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[2], new StoreInfoAndData(new byte[200], 2, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        try (
            Statement statement = store.metadataDb.createStatement();
            ResultSet rs = statement.executeQuery(FilesystemStore.QUERY_GETCURRENT_SIZE)) {
            assertTrue(rs.next());
            assertEquals(1000L, Long.parseLong(rs.getString(1)));
        }

        store.put(keys[4], new StoreInfoAndData(new byte[100], 4, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[0], new StoreInfoAndData(new byte[300], 0, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[1], new StoreInfoAndData(new byte[200], 1, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[3], new StoreInfoAndData(new byte[100], 3, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[2], new StoreInfoAndData(new byte[300], 2, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);


        store.put(keys[2], new StoreInfoAndData(new byte[400], 2, false));
        assertStoreContains(keys, keys[2], keys[3], keys[4]);
    }


    @Test
    public void testDiskSizeRestrictionRemove() throws Exception {
        Key[] keys = prepareDiskSizeRestrictionTests();

        store.put(keys[4], new StoreInfoAndData(new byte[200], 4, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.put(keys[0], new StoreInfoAndData(new byte[200], 0, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        store.remove(keys[1]);
        assertStoreContains(keys, keys[0], keys[2], keys[3], keys[4]);
        store.put(keys[3], new StoreInfoAndData(new byte[400], 3, false));
        assertStoreContains(keys, keys[0], keys[2], keys[3], keys[4]);
        store.put(keys[2], new StoreInfoAndData(new byte[200], 2, false));
        assertStoreContains(keys, keys[0], keys[2], keys[3], keys[4]);
    }

    private Key[] prepareDiskSizeRestrictionTests() throws IOException, SQLException {
        this.store.setMaxSizeKb(1);
        Key[] keys = {new Key(0, "eng", FormatType.html, "full_view", true, FormatterWidth._100),
            new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100),
            new Key(2, "eng", FormatType.html, "full_view", true, FormatterWidth._100),
            new Key(3, "eng", FormatType.html, "full_view", true, FormatterWidth._100),
            new Key(4, "eng", FormatType.html, "full_view", true, FormatterWidth._100),
            new Key(5, "eng", FormatType.html, "full_view", true, FormatterWidth._100)};

        store.put(keys[0], new StoreInfoAndData(new byte[200], 0, false));
        assertStoreContains(keys, keys[0]);
        store.put(keys[1], new StoreInfoAndData(new byte[200], 1, false));
        assertStoreContains(keys, keys[0], keys[1]);
        store.put(keys[2], new StoreInfoAndData(new byte[200], 2, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2]);
        store.put(keys[3], new StoreInfoAndData(new byte[200], 3, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3]);
        store.put(keys[4], new StoreInfoAndData(new byte[200], 4, false));
        assertStoreContains(keys, keys[0], keys[1], keys[2], keys[3], keys[4]);
        return keys;
    }

    private void assertStoreContains(Key[] keys, Key... contained) throws SQLException {
        HashSet<Key> expectedContained = Sets.newHashSet(contained);
        HashSet<Key> actualContained = Sets.newHashSet();

        for (Key key : keys) {
            if (this.store.getInfo(key) != null) {
                actualContained.add(key);
            }
        }

        Sets.SetView<Key> diff = Sets.difference(expectedContained, actualContained);
        assertEquals("Some values were missing: \n" + Joiner.on("\n").join(diff), 0, diff.size());

        Sets.SetView<Key> diff2 = Sets.difference(actualContained, expectedContained);
        assertEquals("Some extra values were found: \n" + Joiner.on("\n").join(diff2), 0, diff2.size());
    }

    @Test
    public void testRemove() throws Exception {
        StoreInfoAndData data = new StoreInfoAndData("result", 10000, true);
        Key key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        assertNotNull(store.get(key));
        assertNotNull(store.getPublished(key));
        assertNotNull(store.getInfo(key));
        assertEquals(1, countFiles(store.getPrivatePath(key)));
        assertEquals(1, countFiles(store.getPublicPath(key)));

        store.remove(key);
        assertNull(store.getInfo(key));
        assertNull(store.getPublished(key));
        assertNull(store.get(key));
        assertEquals(0, countFiles(store.getPrivatePath(key)));
        assertEquals(0, countFiles(store.getPublicPath(key)));

        data = new StoreInfoAndData("result", 10000, false);
        key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        store.put(key, data);
        assertEquals(1, countFiles(store.getPrivatePath(key)));
        assertEquals(0, countFiles(store.getPublicPath(key)));

        store.remove(key);
        assertNull(store.getInfo(key));
        assertNull(store.getPublished(key));
        assertNull(store.get(key));
        assertEquals(0, countFiles(store.getPrivatePath(key)));
        assertEquals(0, countFiles(store.getPublicPath(key)));

        store.remove(key); // no exception ? good
    }

    @Test
    public void testSetPublished() throws Exception {
        StoreInfoAndData data = new StoreInfoAndData("result", 10000, true);
        Key key1 = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        Key key2 = new Key(1, "fre", FormatType.html, "full_view", true, FormatterWidth._100);
        Key key3 = new Key(1, "fre", FormatType.xml, "full_view", true, FormatterWidth._100);
        Key key4 = new Key(1, "fre", FormatType.html, "xml_view", true, FormatterWidth._100);
        Key key5 = new Key(1, "fre", FormatType.html, "xml_view", true, FormatterWidth._100);
        store.put(key1, data);
        store.put(key2, data);
        store.put(key3, data);
        store.put(key4, data);
        store.put(key5, data);

        assertPublished(key1, key2, key3, key4, key5);

        store.setPublished(1, true);
        assertPublished(key1, key2, key3, key4, key5);

        store.setPublished(1, false);
        assertUnpublished(key1, key2, key3, key4, key5);

        store.setPublished(1, false);
        assertUnpublished(key1, key2, key3, key4, key5);

        store.setPublished(1, true);
        assertPublished(key1, key2, key3, key4, key5);

    }

    private void assertUnpublished(Key... keys) throws IOException {
        for (Key key : keys) {
            assertNull(key.toString(), store.getPublished(key));
        }
    }

    private void assertPublished(Key... keys) throws IOException {
        for (Key key : keys) {
            assertNotNull(key.toString(), store.getPublished(key));
        }
    }

    private int countFiles(Path htmlCacheDir) throws IOException {
        final int[] count = {0};
        if (Files.exists(htmlCacheDir)) {
            Files.walkFileTree(htmlCacheDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    count[0] += 1;
                    return super.visitFile(file, attrs);
                }
            });
        }
        return count[0];
    }

    private void initStore() throws SQLException, ClassNotFoundException {
        this.store = new FilesystemStore();
        this.store.setTesting(true);
        store.setGeonetworkDataDir(geonetworkDataDirectory);
    }

    private void createDataDir() {
        this.geonetworkDataDirectory = Mockito.mock(GeonetworkDataDirectory.class);
        this.fileSystem = Jimfs.newFileSystem("blarg", Configuration.unix());
        Mockito.when(geonetworkDataDirectory.getHtmlCacheDir()).thenReturn(fileSystem.getPath("/html"));
    }

}
