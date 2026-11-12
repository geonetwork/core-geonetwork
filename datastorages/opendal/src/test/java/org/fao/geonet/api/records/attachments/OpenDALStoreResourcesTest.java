/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;
import org.apache.opendal.Entry;
import org.apache.opendal.Metadata;
import org.apache.opendal.Operator;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.OpenDALConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Listing a record's resources must not need a stat per file when the listing already carries
 * the size and last modification (one HEAD request per attachment on S3).
 */
public class OpenDALStoreResourcesTest {
    private static final String UUID = "uuid-1";
    private static final int METADATA_ID = 42;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private OpenDALConfiguration config;
    private OpenDALStore store;
    private ServiceContext context;
    private SettingManager settingManager;

    @Before
    public void setUp() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put("root", tmp.newFolder("root").getAbsolutePath());
        config = new OpenDALConfiguration();
        config.setScheme("fs");
        config.setOptions(options);
        config.init();

        store = new OpenDALStore() {
            @Override
            protected int canDownload(ServiceContext context, String metadataUuid,
                                      MetadataResourceVisibility visibility, Boolean approved) {
                return METADATA_ID;
            }

            @Override
            protected String getMetadataDir(int metadataId) {
                return "metadata/" + metadataId;
            }
        };
        settingManager = mock(SettingManager.class);
        when(settingManager.getNodeURL()).thenReturn("http://localhost/geonetwork/");
        ReflectionTestUtils.setField(store, "openDALConfiguration", config);
        ReflectionTestUtils.setField(store, "settingManager", settingManager);
        context = mock(ServiceContext.class);
    }

    @After
    public void tearDown() {
        config.destroy();
    }

    private static Metadata fileMetadata(long size, Instant lastModified) {
        return new Metadata(Metadata.EntryMode.FILE.ordinal(), size, null, null, null, null, null, lastModified, null);
    }

    @Test
    public void listedMetadataUsesTheListingWithoutStat() {
        Operator op = mock(Operator.class);
        Metadata listed = fileMetadata(5, Instant.parse("2019-04-16T16:40:01Z"));

        Metadata result = OpenDALStore.listedMetadata(op, new Entry("metadata/42/a.txt", listed));

        assertSame(listed, result);
        verify(op, never()).stat("metadata/42/a.txt");
    }

    @Test
    public void listedMetadataStatsWhenTheListingHasNoLastModification() {
        Operator op = mock(Operator.class);
        Metadata stat = fileMetadata(5, Instant.parse("2019-04-16T16:40:01Z"));
        when(op.stat("metadata/42/a.txt")).thenReturn(stat);

        Metadata result = OpenDALStore.listedMetadata(op, new Entry("metadata/42/a.txt", fileMetadata(5, null)));

        assertSame(stat, result);
        verify(op).stat("metadata/42/a.txt");
    }

    @Test
    public void getResourcesReturnsSizeAndDate() throws Exception {
        Operator op = config.getOperator();
        op.write("metadata/42/public/a.txt", "12345".getBytes(StandardCharsets.UTF_8));
        op.write("metadata/42/public/b.txt", "1234567".getBytes(StandardCharsets.UTF_8));
        op.write("metadata/42/private/c.txt", "x".getBytes(StandardCharsets.UTF_8));

        List<MetadataResource> resources = store.getResources(context, UUID, MetadataResourceVisibility.PUBLIC,
            null, true, false);

        assertEquals(2, resources.size());
        assertEquals("uuid-1/attachments/a.txt", resources.get(0).getId());
        assertEquals(5, resources.get(0).getSize());
        assertNotNull(resources.get(0).getLastModification());
        assertEquals("uuid-1/attachments/b.txt", resources.get(1).getId());
        assertEquals(7, resources.get(1).getSize());
        assertNotNull(resources.get(1).getLastModification());
    }

    @Test
    public void getResourcesReadsTheNodeUrlOncePerListing() throws Exception {
        Operator op = config.getOperator();
        for (int i = 0; i < 20; i++) {
            op.write("metadata/42/public/f" + i + ".txt", "x".getBytes(StandardCharsets.UTF_8));
        }

        List<MetadataResource> resources = store.getResources(context, UUID, MetadataResourceVisibility.PUBLIC,
            null, true, false);

        assertEquals(20, resources.size());
        assertEquals("http://localhost/geonetwork/api/records/uuid-1/attachments/f0.txt", resources.get(0).getUrl());
        verify(settingManager, times(1)).getNodeURL();
    }
}
