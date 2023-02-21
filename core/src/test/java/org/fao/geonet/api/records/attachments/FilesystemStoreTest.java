/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Date;

import static org.junit.Assert.*;

public class FilesystemStoreTest extends AbstractCoreIntegrationTest {

    @Autowired
    protected IMetadataManager metadataManager;
    @Autowired
    protected SettingManager settingManager;

    @Test
    public void getResourceDescription() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String mdId = metadataManager.insertMetadata(
            context,
            "iso19139",
            new Element("MD_Metadata"),
            "uuid",
            context.getUserSession().getUserIdAsInt(),
            "" + ReservedGroup.all.getId(),
            "sourceid",
            "n",
            "doctype",
            null,
            new ISODate().getDateAndTime(),
            new ISODate().getDateAndTime(),
            false,
            IndexingMode.none);

        FilesystemStore filesystemStore = new FilesystemStore();
        filesystemStore.settingManager = this.settingManager;

        MetadataResource resource = filesystemStore.getResourceDescription(context, "uuid", MetadataResourceVisibility.PUBLIC, "test.jpg", true);
        assertNull("Non exising resource must be null", resource);

        try (InputStream file = this.getClass().getResourceAsStream("existingResource.jpg")) {
            filesystemStore.putResource(context, "uuid", "existingResource.jpg", file, new Date(),
                MetadataResourceVisibility.PUBLIC, true);
            resource = filesystemStore.getResourceDescription(context, "uuid",
                MetadataResourceVisibility.PUBLIC, "existingResource.jpg", true);
            assertNotNull("Existing resource must not return null", resource);
            assertEquals("The file size doesn't match the expected one", 6416, resource.getSize());
            assertEquals("The visibility must be public", MetadataResourceVisibility.PUBLIC, resource.getVisibility());
            assertEquals("Filename is wrong", "existingResource.jpg", resource.getFilename());
            assertEquals("Metadata id is wrong", Integer.parseInt(mdId), resource.getMetadataId());
        }
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetResourceDescriptionNonExistingUuid() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        FilesystemStore filesystemStore = new FilesystemStore();
        filesystemStore.settingManager = this.settingManager;


        // context, metadataUuid, visibility, path, approved)
        filesystemStore.getResourceDescription(context, "nonExistingUuid",
            MetadataResourceVisibility.PUBLIC, "existingResource.jpg", true);
    }
}
