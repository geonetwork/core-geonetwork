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
package org.fao.geonet.services.metadata;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fao.geonet.kernel.BatchEditParameter;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Attribute;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import jeeves.server.context.ServiceContext;

public class BatchEditsServiceTest extends AbstractServiceIntegrationTest {

    List<String> uuids = new ArrayList();
    String firstMetadataId = null;
    ServiceContext context;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private IMetadataUtils repository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void loadSamples() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.getMefFilesToLoad().add("test-iso19115-3.zip");
        importMetadata.invoke();
        List<String> importedRecordUuids = importMetadata.getMetadataIds();

        // Check record are imported
        for (String id : importedRecordUuids) {
            final String uuid = repository.findOne(Integer.valueOf(id)).getUuid();
            uuids.add(uuid);
            if (firstMetadataId == null) {
                firstMetadataId = uuid;
            }
        }
        assertEquals(4, repository.count());
    }


    @Test
    public void testParameterMustBeSet() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final BatchEditParameter[] parameters = new BatchEditParameter[]{};
        Gson gson = new GsonBuilder()
                .create();
        JsonElement jsonEl = gson.toJsonTree(parameters);

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that edit must be defined
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + firstMetadataId)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(jsonPath("$.message", is("At least one edit must be defined.")))
                .andExpect(status().is(400));

    }


    @Test
    public void testUpdateRecord() throws Exception {
        final BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                    "gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                "## UPDATED TITLE ##"
            ),
            new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                    "gmd:abstract/gco:CharacterString",
                "## UPDATED ABSTRACT ##"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
                .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        // Check 201 is returned
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + firstMetadataId)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(firstMetadataId);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        for (BatchEditParameter p : listOfupdates) {
            assertEqualsText(p.getValue(),
                xml,
                p.getXpath(),
                GMD, GCO);
        }
    }


    @Test
    public void testUpdateRecordElementInsertWithParentXpath() throws Exception {
        final String uuid = "db07463b-6769-401e-944b-f22e2e3bcc26";
        // XPath has no match and same type as fragment, element is created
        BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:distributionInfo/mrd:MD_Distribution",
                "<gn_add><mrd:distributor xmlns:mrd=\"http://standards.iso.org/iso/19115/-3/mrd/1.0\"/></gn_add>"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
            .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(uuid);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        List nodes = org.fao.geonet.utils.Xml.selectNodes(xml, "./mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor", xml.getAdditionalNamespaces());
        Assert.assertEquals(1, nodes.size());
    }


    @Test
    public void testUpdateRecordUpdateAttribute() throws Exception {
        final String uuid = "db07463b-6769-401e-944b-f22e2e3bcc26";
        BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:MD_Metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@codeListValue",
                "newScope"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
            .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(uuid);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        List attr = org.fao.geonet.utils.Xml.selectNodes(xml,
            "./mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@codeListValue",
            xml.getAdditionalNamespaces());
        Assert.assertEquals("newScope", ((Attribute) attr.get(0)).getValue());


        listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:MD_Metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@codeListValue",
                "<gn_replace>anotherNewScope</gn_replace>"
            )
        };

        jsonEl = gson.toJsonTree(listOfupdates);

        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        updatedRecord = repository.findOneByUuid(uuid);
        xml = Xml.loadString(updatedRecord.getData(), false);

        attr = org.fao.geonet.utils.Xml.selectNodes(xml,
            "./mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@codeListValue",
            xml.getAdditionalNamespaces());
        Assert.assertEquals("anotherNewScope", ((Attribute) attr.get(0)).getValue());
    }


    @Test
    public void testUpdateRecordDeleteAttribute() throws Exception {
        final String uuid = "db07463b-6769-401e-944b-f22e2e3bcc26";
        BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:MD_Metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@codeListValue",
                "<gn_delete/>"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
            .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(uuid);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        List scope = org.fao.geonet.utils.Xml.selectNodes(xml,
            "./mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope",
            xml.getAdditionalNamespaces());
        Assert.assertEquals(null, ((Element) scope.get(0)).getAttribute("codeListValue"));
    }


    @Test
    public void testUpdateRecordAddAttribute() throws Exception {
        final String uuid = "db07463b-6769-401e-944b-f22e2e3bcc26";
        BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:MD_Metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/@newAttribute",
                "<gn_add>value</gn_add>"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
            .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(uuid);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        List scope = org.fao.geonet.utils.Xml.selectNodes(xml,
            "./mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope",
            xml.getAdditionalNamespaces());
        Assert.assertEquals("value", ((Element) scope.get(0)).getAttributeValue("newAttribute"));
    }

    @Test
    public void testUpdateRecordElement() throws Exception {
        final String uuid = "db07463b-6769-401e-944b-f22e2e3bcc26";
        // XPath has no match and same type as fragment, element is created
        BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor[1]",
                "<gn_add><mrd:distributor xmlns:mrd=\"http://standards.iso.org/iso/19115/-3/mrd/1.0\"><mrd:MD_Distributor/></mrd:distributor></gn_add>"
            )
        };

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        Gson gson = new GsonBuilder()
            .create();
        JsonElement jsonEl = gson.toJsonTree(listOfupdates);

        // Check 201 is returned
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        AbstractMetadata updatedRecord = repository.findOneByUuid(uuid);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        List nodes = org.fao.geonet.utils.Xml.selectNodes(xml, "./mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor", xml.getAdditionalNamespaces());
        Assert.assertEquals(1, nodes.size());


        // Added again
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        updatedRecord = repository.findOneByUuid(uuid);
        xml = Xml.loadString(updatedRecord.getData(), false);

        nodes = org.fao.geonet.utils.Xml.selectNodes(xml, "./mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor", xml.getAdditionalNamespaces());
        Assert.assertEquals(2, nodes.size());


        // Added in parent
        listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:distributionInfo/mrd:MD_Distribution",
                "<gn_add><mrd:distributor xmlns:mrd=\"http://standards.iso.org/iso/19115/-3/mrd/1.0\"/></gn_add>"
            )
        };
        gson = new GsonBuilder().create();
        jsonEl = gson.toJsonTree(listOfupdates);
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        updatedRecord = repository.findOneByUuid(uuid);
        xml = Xml.loadString(updatedRecord.getData(), false);

        nodes = org.fao.geonet.utils.Xml.selectNodes(xml, "./mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor", xml.getAdditionalNamespaces());
        Assert.assertEquals(3, nodes.size());


        // Inserting element at position 1
        listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "/mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor[1]",
                "<gn_add><mrd:distributor flag=\"Inserting element at position\" xmlns:mrd=\"http://standards.iso.org/iso/19115/-3/mrd/1.0\"/></gn_add>"
            )
        };
        gson = new GsonBuilder().create();
        jsonEl = gson.toJsonTree(listOfupdates);
        this.mockMvc.perform(put("/srv/api/records/batchediting?uuids=" + uuid)
                .content(jsonEl.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        updatedRecord = repository.findOneByUuid(uuid);
        xml = Xml.loadString(updatedRecord.getData(), false);

        nodes = org.fao.geonet.utils.Xml.selectNodes(xml, "./mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor", xml.getAdditionalNamespaces());
        Assert.assertEquals(4, nodes.size());
        Assert.assertEquals("Inserting element at position",
            ((Element) nodes.get(0)).getAttributeValue("flag"));
    }
}
