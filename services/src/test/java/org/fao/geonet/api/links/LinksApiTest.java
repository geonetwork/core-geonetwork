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
package org.fao.geonet.api.links;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LinksApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private IMetadataUtils metadataRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private MockMvc mockMvc;
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    @Test
    public void getLinksAsAdmin() throws Exception {
        AbstractMetadata md = createMd(createGroupWithOneEditor(createEditor()).getId());

        analyzeMdAsAdmin(md);
        Assert.assertEquals(1, linkRepository.count());

        assertLinkForOneMdFound(md, this.loginAsAdmin());

        purgeLinkAsAdmin();
    }

    @Test
    public void getLinksAsEditor() throws Exception {
        User editor = createEditor();
        AbstractMetadata md = createMd(createGroupWithOneEditor(editor).getId());
        analyzeMdAsAdmin(md);

        assertLinkForOneMdFound(md, this.loginAs(editor));

        purgeLinkAsAdmin();
    }

    @Test
    public void getLinksAsEditorFromAnotherGroup() throws Exception {
        User editor = createEditor();
        createGroupWithOneEditor(editor);
        AbstractMetadata md = createMd(createGroupWithOneEditor(createEditor()).getId());
        analyzeMdAsAdmin(md);

        assertNoLinksReturned(this.loginAs(editor));

        purgeLinkAsAdmin();
        }

    @Test
    public void getLinksAsEditorFromNoGroup() throws Exception {
        User editor = createEditor();
        AbstractMetadata md = createMd(createGroupWithOneEditor(createEditor()).getId());
        analyzeMdAsAdmin(md);

        assertNoLinksReturned(this.loginAs(editor));

        purgeLinkAsAdmin();
    }

    @Test
    public void getLinksAsAdminWithATwiceUsedLink() throws Exception {
        AbstractMetadata md = createMd(createGroupWithOneEditor(createEditor()).getId());
        AbstractMetadata md1 = createMd(createGroupWithOneEditor(createEditor()).getId());

        analyzeMdAsAdmin(md);
        analyzeMdAsAdmin(md1);
        Assert.assertEquals(1, linkRepository.count());

        MockHttpSession httpSession = this.loginAsAdmin();
        this.mockMvc.perform(get("/srv/api/records/links")
                .session(httpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].url").value(equalTo("http://services.sandre.eaufrance.fr/geo/ouvrage")))
                .andExpect(jsonPath("$.content[0].records", hasSize(2)))
                .andExpect(jsonPath("$.content[0].records[0].metadataId").value(equalTo(md.getId())))
                .andExpect(jsonPath("$.content[0].records[0].metadataUuid").value(equalTo(md.getUuid())))
                .andExpect(jsonPath("$.content[0].records[1].metadataId").value(equalTo(md1.getId())))
                .andExpect(jsonPath("$.content[0].records[1].metadataUuid").value(equalTo(md1.getUuid())));

        purgeLinkAsAdmin();
    }

    private AbstractMetadata createMd(Integer groupOwner) throws Exception {
        loginAsAdmin(context);

        Element sampleMetadataXml = getSampleMetadataXml();
        String uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(uuid);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml)
                .setUuid(uuid);
        metadata.getDataInfo()
                .setRoot(sampleMetadataXml.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(sampleMetadataXml))
                .setType(MetadataType.METADATA)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(1)
                .setSourceId(sourceRepository.findAll().get(0).getUuid())
                .setGroupOwner(groupOwner);
        metadata.getHarvestInfo().setHarvested(false);

        return dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, true, false, NO,false, false);
    }

    private Group createGroupWithOneEditor(User editor) throws IOException {
        Group group = new Group()
                .setName(UUID.randomUUID().toString().replace("-", ""));
        group = groupRepository.save(group);

        UserGroup userGroup = new UserGroup()
                .setGroup(group)
                .setUser(editor)
                .setProfile(Profile.Editor);
        userGroupRepository.save(userGroup);

        return group;
    }

    private User createEditor() {
        User editor = new User()
                .setUsername(UUID.randomUUID().toString())
                .setProfile(Profile.Reviewer).setName(UUID.randomUUID().toString())
                .setEnabled(true);
        return userRepository.save(editor);
    }

    private void analyzeMdAsAdmin(AbstractMetadata md) throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(post("/srv/api/records/links?uuid=" + md.getUuid())
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated());
    }

    private void purgeLinkAsAdmin() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        this.mockMvc.perform(delete("/srv/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());
        Assert.assertEquals(0, linkRepository.count());
    }

    private void assertLinkForOneMdFound(AbstractMetadata md, MockHttpSession httpSession) throws Exception {
        this.mockMvc.perform(get("/srv/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].url").value(equalTo("http://services.sandre.eaufrance.fr/geo/ouvrage")))
            .andExpect(jsonPath("$.content[0].records", hasSize(1)))
            .andExpect(jsonPath("$.content[0].records[0].metadataId").value(equalTo(md.getId())))
            .andExpect(jsonPath("$.content[0].records[0].metadataUuid").value(equalTo(md.getUuid())));
    }

    private void assertNoLinksReturned(MockHttpSession httpSession) throws Exception {
        this.mockMvc.perform(get("/srv/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
