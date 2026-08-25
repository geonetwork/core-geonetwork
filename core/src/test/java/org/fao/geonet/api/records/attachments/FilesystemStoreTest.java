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
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class FilesystemStoreTest extends AbstractCoreIntegrationTest {

    @Autowired
    protected IMetadataManager metadataManager;
    @Autowired
    protected IMetadataUtils metadataUtils;
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

    /**
     * #9433 - the {@code approved} flag returned for each resource must reflect whether the record
     * is a draft (i.e. whether an approved copy exists), not merely the {@code approved} request
     * parameter. Requesting {@code approved=true} must not mark a draft record's resources as
     * approved.
     */
    @Test
    public void getResourcesApprovedFlagReflectsDraftState() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String uuid = "9433-uuid";
        String mdId = metadataManager.insertMetadata(
            context, "iso19139", new Element("MD_Metadata"), uuid,
            context.getUserSession().getUserIdAsInt(), "" + ReservedGroup.all.getId(),
            "sourceid", "n", "doctype", null,
            new ISODate().getDateAndTime(), new ISODate().getDateAndTime(), false, IndexingMode.none);

        FilesystemStore store = new FilesystemStore();
        store.settingManager = this.settingManager;

        try (InputStream file = getClass().getResourceAsStream("existingResource.jpg")) {
            store.putResource(context, uuid, "existingResource.jpg", file, new Date(),
                MetadataResourceVisibility.PUBLIC, true);
        }

        // The approved flag must follow the record's actual draft state, not the request parameter.
        boolean isDraft = metadataUtils.isMetadataDraft(Integer.parseInt(mdId));
        assertEquals("approvedCopyExists must be the inverse of the record's draft state",
            !isDraft, AbstractStore.approvedCopyExists(uuid));

        List<MetadataResource> resources = store.getResources(context, uuid, Sort.name, null, true);
        assertFalse("The uploaded resource must be listed", resources.isEmpty());
        assertEquals("Even with approved=true, the approved flag must reflect the draft state (#9433)",
            !isDraft, resources.get(0).isApproved());

        // approved=false is always resolved as not approved.
        assertFalse("approved=false must never resolve to approved",
            AbstractStore.resolveApproved(uuid, false));

        // An unknown record has no approved copy.
        assertFalse("No approved copy exists for an unknown record",
            AbstractStore.approvedCopyExists("9433-missing-uuid"));
    }

    @Test
    public void testGetFilenameFromUrl() throws Exception {
        FilesystemStore filesystemStore = new FilesystemStore();

        String fileName = filesystemStore.getFilenameFromUrl(new URL("http://mydomain.com/filename.txt"));
        assertEquals("filename.txt", fileName);

        fileName = filesystemStore.getFilenameFromUrl(new URL("http://mydomain.com/filename%20with%20spaces.txt"));
        assertEquals("filename with spaces.txt", fileName);

        fileName = filesystemStore.getFilenameFromUrl(new URL("http://mydomain.com/filename.txt?param=value"));
        assertEquals("filename.txt", fileName);

        fileName = filesystemStore.getFilenameFromUrl(new URL("http://mydomain.com/filename%20with%20spaces.txt?param=value"));
        assertEquals("filename with spaces.txt", fileName);
    }
}
