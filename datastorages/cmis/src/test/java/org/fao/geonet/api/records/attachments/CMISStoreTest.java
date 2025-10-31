/*
 * =============================================================================
 * ===	Copyright (C) 2019 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * This test needs a configuration placed in services/src/test/resources/jcloud-test-context.xml to run:
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *  &lt;beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *         default-lazy-init="true"
 *         xmlns="http://www.springframework.org/schema/beans"
 *         xsi:schemaLocation="
 *          http://www.springframework.org/schema/beans
 *          http://www.springframework.org/schema/beans/spring-beans.xsd
 *         "&gt;
 *
 *     &lt;bean id="jCloudStore" class="org.fao.geonet.api.records.attachments.JCloudStore"&gt;
 *       &lt;property name="provider" value="azureblob"/&gt;
 *       &lt;property name="containerName" value="geonetwork-test"/&gt;
 *       &lt;property name="baseFolder" value="geonetwork"/&gt;
 *       &lt;property name="storageAccountName" value="MyAccessKey"/&gt;
 *       &lt;property name="storageAccountKey" value="MySecretKey"/&gt;
 *     &lt;/bean&gt;
 *  &lt;/beans&gt;
 * </pre>
 */
public class CMISStoreTest extends AbstractStoreTest {
    @Autowired
    private GenericApplicationContext context;

    @Autowired(required = false)
    private CMISStore store;

    @Override
    protected Store getStore() {
        Assume.assumeTrue("Cannot load cmis-test-context.xml => skipped test", store != null);
        return store;
    }

    @Override
    public void testGetResources() throws Exception {
        super.testGetResources();
    }

    @Override
    public void testPutPatchAndDeleteResource() throws Exception {
        super.testPutPatchAndDeleteResource();
    }

    @Override
    public void testPutResourceFromURL() throws Exception {
        super.testPutResourceFromURL();
    }

    @Override
    public void testPutResourceFromURLWithURLParameters() throws Exception {
        super.testPutResourceFromURLWithURLParameters();
    }

    @Test
    public void testCopyResourcesShouldResetMetadataUuid() throws Exception {
        CMISStore cmisStoreSpy = spy(store);

        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        // Import source metadata
        String sourceMetadataId = importMetadata(context);
        String sourceMetadataUuid = metadataUtils.getMetadataUuid(sourceMetadataId);

        // Import target metadata
        String targetMetadataId = importMetadata(context);
        String targetMetadataUuid = metadataUtils.getMetadataUuid(targetMetadataId);

        // Clean up any existing resources
        cmisStoreSpy.delResources(context, sourceMetadataUuid, true);
        cmisStoreSpy.delResources(context, targetMetadataUuid, true);

        // Add a resource to source metadata
        String filename = "record-with-old-links.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(Paths.get(resources, filename)));

        cmisStoreSpy.putResource(context, sourceMetadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        // Copy resources from source to target
        cmisStoreSpy.copyResources(context, sourceMetadataUuid, targetMetadataUuid,
            MetadataResourceVisibility.PUBLIC, true, true);

        verify(cmisStoreSpy).setCmisMetadataUUIDPrimary(any(), any());

        // Clean up
        cmisStoreSpy.delResources(context, sourceMetadataUuid, true);
        cmisStoreSpy.delResources(context, targetMetadataUuid, true);
    }
}

