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
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.XmlSerializerIntegrationTest;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataManager;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link DraftMetadataUtils}.
 *
 * @author delawen María Arias de Reyna
 */
@ContextConfiguration(inheritLocations = true, locations = {"classpath:draft-test-context.xml"})
public class DraftMetadataUtilsTest extends AbstractCoreIntegrationTest {

    private static final String UUID = "test-md" + Math.random();

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private IMetadataStatus metadataStatus;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    private User user;
    private Group group;
    private AbstractMetadata md;

    @Before
    public void init() throws IOException {
        user = new User();
        user.setUsername(UUID);
        user.setProfile(Profile.Reviewer);
        user.setName(UUID);
        user.setEnabled(true);
        user = userRepository.save(user);

        group = new Group();
        group.setName(UUID);
        group = groupRepository.save(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroup(group);
        userGroup.setUser(user);
        userGroup.setProfile(Profile.Reviewer);
        userGroupRepository.save(userGroup);

        md = metadataManager.save(createMetadata());
    }

    @Test
    public void usingDraftUtilities() {
        assertTrue(metadataManager instanceof DraftMetadataManager);
        assertTrue(metadataUtils instanceof DraftMetadataUtils);
    }

    @Autowired
    private SettingManager settingManager;

    @Test
    public void testEditing() throws Exception {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, "true");
        ServiceContext context = createServiceContext();
        loginAs(user);

        assertTrue(metadataUtils.findOne(md.getId()) instanceof Metadata);

        HashSet<Integer> set = new HashSet<Integer>();
        set.add(md.getId());
        Iterable<? extends AbstractMetadata> mds = metadataUtils.findAll(set);
        Iterator<? extends AbstractMetadata> it = mds.iterator();

        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());

        Integer id = metadataUtils.startEditingSession(context, String.valueOf(md.getId()));

        assertNotNull(id);

        metadataStatus.setStatus(context, id, Integer.valueOf(StatusValue.Status.APPROVED), new ISODate(),
            "Approve record");
        id = metadataUtils.startEditingSession(context, String.valueOf(md.getId()));

        assertTrue(id != md.getId());
        assertTrue(metadataUtils.findOne(id) instanceof MetadataDraft);

        metadataUtils.cancelEditingSession(context, id.toString());

        Integer id2 = metadataUtils.startEditingSession(context, String.valueOf(md.getId()));

        assertNotNull(id2);
        assertEquals(id, id2);

        metadataUtils.endEditingSession(id.toString(), context.getUserSession());

        id2 = metadataUtils.startEditingSession(context, String.valueOf(md.getId()));

        assertNotNull(id2);
        assertEquals(id, id2);

        metadataUtils.endEditingSession(id.toString(), context.getUserSession());

        assertTrue(metadataUtils.findOneByUuid(UUID) instanceof MetadataDraft);

    }

    private Metadata createMetadata() throws IOException {
        Metadata md = new Metadata();
        md.setUuid(UUID);
        try (InputStream is = XmlSerializerIntegrationTest.class.getResourceAsStream("valid-metadata.iso19139.xml")) {
            md.setData(IOUtils.toString(is));
        }
        md.getSourceInfo().setGroupOwner(group.getId());
        md.getSourceInfo().setOwner(user.getId());
        md.getSourceInfo().setSourceId("test-faking");
        md.getDataInfo().setSchemaId("iso19139");
        md.getDataInfo().setType(MetadataType.TEMPLATE);
        return md;
    }

    @Test
    public void auxiliaryFunctions() throws Exception {
        assertNull(metadataUtils.getMetadataId("-1"));
        assertTrue(Integer.valueOf(metadataUtils.getMetadataId(md.getUuid())) == md.getId());

        Map<Integer, MetadataSourceInfo> res = metadataUtils.findAllSourceInfo(MetadataSpecs.hasMetadataId(md.getId()));
        assertNotNull(res);
        assertFalse(res.isEmpty());
    }

    @Test
    public void getMetadataIdNotExist() throws Exception {
        assertNull(metadataUtils.getMetadataId("-1"));
    }

    @After
    public void cleanup() throws Exception {

        while(metadataUtils.existsMetadataUuid(UUID)) {
            AbstractMetadata md = metadataUtils.findOneByUuid(UUID);
            metadataManager.delete(md.getId());
        }

        userRepository.delete(user.getId());
        groupRepository.delete(group.getId());
    }

}
