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

package org.fao.geonet.kernel;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.springframework.data.jpa.domain.Specification.where;

public class DataManagerIntegrationTest extends AbstractDataManagerIntegrationTest {

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    public void testDeleteMetadata() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        long count = metadataRepository.count();

        int mdId = injectMetadataInDb(getSampleMetadataXml(), serviceContext).getId();

        assertEquals(count + 1, metadataRepository.count());

        metadataManager.deleteMetadata(serviceContext, String.valueOf(mdId));

        assertEquals(count, metadataRepository.count());
    }

    @Test
    public void testBuildPrivilegesMetadataInfo() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        Element sampleMetadataXml = getSampleMetadataXml();
        String mdId = dataManager.insertMetadata(
                serviceContext,
                dataManager.autodetectSchema(sampleMetadataXml),
                new Element(sampleMetadataXml.getName(), sampleMetadataXml.getNamespace()),
                "uuid",
                serviceContext.getUserSession().getUserIdAsInt(),
                "" + ReservedGroup.all.getId(),
                "sourceid",
                "n",
                "doctype",
                null,
                new ISODate().getDateAndTime(),
                new ISODate().getDateAndTime(),
                false,
                IndexingMode.none);
        Element info = new Element("info", Geonet.Namespaces.GEONET);
        Map<String, Element> map = Maps.newHashMap();
        map.put(mdId, info);
        info.removeContent();

        dataManager.buildPrivilegesMetadataInfo(serviceContext, map);

        assertEqualsText("true", info, "edit");
        assertEqualsText("true", info, "owner");
        assertEqualsText("true", info, "isPublishedToAll");
        assertEqualsText("true", info, "view");
        assertEqualsText("true", info, "notify");
        assertEqualsText("true", info, "download");
        assertEqualsText("true", info, "dynamic");
        assertEqualsText("true", info, "featured");
    }

    @Test
    public void testCreateMetadataWithTemplateMetadata() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        User principal = serviceContext.getUserSession().getPrincipal();
        Group group = groupRepository.findAll().get(0);
        MetadataCategory category = metadataCategoryRepository.findAll().get(0);
        Source source = sourceRepository.save(new Source().setType(SourceType.portal).setName("GN").setUuid("sourceuuid"));

        Element sampleMetadataXml = super.getSampleMetadataXml();
        AbstractMetadata metadata = new Metadata()
                .setDataAndFixCR(sampleMetadataXml)
                .setUuid(UUID.randomUUID().toString());
        metadata.getCategories().add(category);
        metadata.getDataInfo().setSchemaId("iso19139");
        metadata.getSourceInfo().setSourceId(source.getUuid()).setOwner(1);

        AbstractMetadata templateMd = metadataManager.save(metadata);
        String newMetadataId = dataManager.createMetadata(serviceContext, "" + metadata.getId(), "" + group.getId(), source.getUuid(),
            principal.getId(), templateMd.getUuid(), MetadataType.METADATA.codeString, true);

        AbstractMetadata newMetadata = metadataRepository.findById(Integer.parseInt(newMetadataId)).get();
        assertEquals(1, newMetadata.getCategories().size());
        assertEquals(category, newMetadata.getCategories().iterator().next());
        assertEqualsText(metadata.getUuid(), newMetadata.getXmlData(false), "gmd:parentIdentifier/gco:CharacterString");

    }

    @Test
    public void testSetStatus() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        int metadataId = importMetadata(serviceContext);
        assertEquals(null, dataManager.getStatus(metadataId));

        ISODate changeDate = new ISODate();
        String changeMessage = "Set to draft";
        dataManager.setStatus(serviceContext, metadataId, 0, changeDate, changeMessage);

        MetadataStatus loadedStatus = dataManager.getStatus(metadataId);
        assertEquals(changeDate, loadedStatus.getChangeDate());
        assertEquals(changeMessage, loadedStatus.getChangeMessage());
        assertEquals(0, loadedStatus.getStatusValue().getId());
        assertEquals(metadataId, loadedStatus.getMetadataId());
        assertEquals(0, loadedStatus.getStatusValue().getId());
        assertEquals(serviceContext.getUserSession().getUserIdAsInt(), loadedStatus.getUserId());
    }

    @Test
    public void testSetHarvesterData() throws Exception {
        doSetHarvesterDataTest();
    }

    @Test
    public void testDeleteBatchMetadata() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        Specification<Metadata> spec = where((Specification<Metadata>)MetadataSpecs.hasMetadataUuid(uuid1)).or((Specification<Metadata>)MetadataSpecs.hasMetadataUuid(uuid2));
        importMetadata(serviceContext, uuid1);
        importMetadata(serviceContext, uuid2);
        assertEquals(2, metadataRepository.findAll(spec).size());
        assertEquals(2, searchManager.query(String.format("uuid:(%s OR %s)", uuid1, uuid2), null, 0, 10).hits().hits().size());

        dataManager.batchDeleteMetadataAndUpdateIndex(spec);

        assertEquals(0, metadataRepository.findAll(spec).size());
        assertEquals(0, searchManager.query(String.format("uuid:(%s OR %s)", uuid1, uuid2), null, 0, 10).hits().hits().size());
    }

    @Test
    public void testUpdateFixedInfo() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        Element md = Xml.loadFile(AbstractCoreIntegrationTest.class.getResource("kernel/multilingual-metadata.xml"));

        String uuid = UUID.randomUUID().toString();
        String parentUuid = UUID.randomUUID().toString();
        Element updateMd = dataManager.updateFixedInfo("iso19139", Optional.<Integer>absent(), uuid, md, parentUuid,
            UpdateDatestamp.YES, serviceContext);

        List<Namespace> namespaces = dataManager.getSchema("iso19139").getNamespaces();
        assertEquals(uuid, Xml.selectString(updateMd, "gmd:fileIdentifier/gco:CharacterString", namespaces));
        assertEquals(parentUuid, Xml.selectString(updateMd, "gmd:parentIdentifier/gco:CharacterString", namespaces));
        assertEquals(0, Xml.selectNodes(updateMd, "*//node()[string-length(@locale) > 3]").size());
        assertEquals(0, Xml.selectNodes(updateMd, "*//gmd:PT_Locale[string-length(@id) > 2]").size());
    }

}
