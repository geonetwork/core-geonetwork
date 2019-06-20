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
package org.fao.geonet.kernel.datamanager;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.XmlSerializerIntegrationTest;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataManager;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link DraftMetadataManager} and {@link DraftMetadataUtils}.
 *
 * @author delawen María Arias de Reyna
 */
@ContextConfiguration(inheritLocations = true, locations = {"classpath:draft-test-context.xml"})
public class DraftMetadataManagerTest extends AbstractCoreIntegrationTest {

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private User user;
    private Group group;
    private AbstractMetadata md;

    @Before
    public void init() {

        user = userRepository.findAll().get(0);

        for (Group g : groupRepository.findAll()) {
            if (!g.isReserved()) {
                group = g;
                break;
            }
        }
    }

    @Test
    public void usingDraftUtilities() {
        assertTrue(metadataManager instanceof DraftMetadataManager);
        assertTrue(metadataUtils instanceof DraftMetadataUtils);
    }

    @Test
    public void testSave() throws Exception {
        assertTrue(metadataUtils.findAll(MetadataSpecs.hasType(MetadataType.TEMPLATE)).isEmpty());

        Metadata record = createMetadata();
        record = (Metadata) metadataManager.save(record);
        
        assertNotNull(record);
        assertNotNull(record.getId());
        
        md = metadataManager.save(createMetadataDraft(record));

        assertNotNull(md);
        assertNotNull(metadataUtils.findOne(md.getId()));
        assertTrue(metadataUtils.exists(md.getId()));
        assertTrue(metadataUtils.existsMetadata(md.getId()));
        assertTrue(metadataUtils.existsMetadataUuid(md.getUuid()));

        metadataManager.delete(md.getId());

        assertNull(metadataUtils.findOne(md.getId()));
        assertFalse(metadataUtils.exists(md.getId()));
        assertFalse(metadataUtils.existsMetadata(md.getId()));
        
        assertTrue(metadataUtils.existsMetadataUuid(md.getUuid()));
        metadataManager.delete(record.getId());
        assertFalse(metadataUtils.existsMetadataUuid(md.getUuid()));
      
    }

    /**
     * Should behave like the same function on {@link BaseMetadataManagerTest}
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        md = metadataManager.save(createMetadata());

        List<? extends AbstractMetadata> templates = metadataUtils
            .findAll(MetadataSpecs.hasType(MetadataType.TEMPLATE));

        assertFalse(templates.isEmpty());

        String id = metadataManager.createMetadata(context, String.valueOf(templates.get(0).getId()),
            String.valueOf(group.getId()), "test", user.getId(), null, MetadataType.METADATA.codeString, true);

        assertNotNull(id);
        assertTrue(metadataUtils.exists(md.getId()));
        assertTrue(metadataUtils.existsMetadata(md.getId()));
        assertTrue(metadataUtils.existsMetadataUuid(md.getUuid()));

        assertNotNull(metadataUtils.findOne(id));

        metadataManager.delete(Integer.valueOf(id));

        assertNull(metadataUtils.findOne(id));

        metadataManager.delete(md.getId());

        assertNull(metadataUtils.findOne(md.getId()));
        assertFalse(metadataUtils.exists(md.getId()));
        assertFalse(metadataUtils.existsMetadata(md.getId()));
        assertFalse(metadataUtils.existsMetadataUuid(md.getUuid()));
    }

    @Test
    public void testSpecifications() throws Exception {

        assertTrue(metadataUtils.findAll(MetadataSpecs.hasType(MetadataType.TEMPLATE)).isEmpty());

        md = metadataManager.save(createMetadata());

        assertFalse(metadataUtils.findAll(MetadataSpecs.hasType(MetadataType.TEMPLATE)).isEmpty());

        assertFalse(metadataUtils.findAllIdsBy(MetadataSpecs.hasType(MetadataType.TEMPLATE)).isEmpty());

        assertFalse(metadataUtils.findAll(MetadataSpecs.hasMetadataId(md.getId())).isEmpty());
    }

    private Metadata createMetadata() throws IOException {
    	Metadata md = new Metadata();
        md.setUuid("test-metadata");
        try (InputStream is = XmlSerializerIntegrationTest.class.getResourceAsStream("valid-metadata.iso19139.xml")) {
            md.setData(IOUtils.toString(is));
        }
        md.getSourceInfo().setGroupOwner(group.getId());
        md.getSourceInfo().setOwner(1);
        md.getSourceInfo().setSourceId("test-faking");
        md.getDataInfo().setSchemaId("iso19139");
        md.getDataInfo().setType(MetadataType.TEMPLATE);
        return md;
    }


    private AbstractMetadata createMetadataDraft(Metadata record) throws IOException {
    	MetadataDraft md = new MetadataDraft();
        md.setUuid("test-metadata");
        md.setApprovedVersion(record);
        try (InputStream is = XmlSerializerIntegrationTest.class.getResourceAsStream("valid-metadata.iso19139.xml")) {
            md.setData(IOUtils.toString(is));
        }
        md.getSourceInfo().setGroupOwner(group.getId());
        md.getSourceInfo().setOwner(1);
        md.getSourceInfo().setSourceId("test-faking");
        md.getDataInfo().setSchemaId("iso19139");
        md.getDataInfo().setType(MetadataType.TEMPLATE);
        return md;
    }

    @After
    public void cleanup() {

    }

}
