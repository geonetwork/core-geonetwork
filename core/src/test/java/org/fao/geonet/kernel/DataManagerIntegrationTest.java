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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Tests for the DataManager.
 * <p/>
 * User: Jesse Date: 10/24/13 Time: 5:30 PM
 */
public class DataManagerIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    DataManager _dataManager;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    MetadataRepository _metadataRepository;

    static void doSetHarvesterDataTest(MetadataRepository metadataRepository, DataManager dataManager, int metadataId) throws Exception {
        AbstractMetadata metadata = metadataRepository.findOne(metadataId);

        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());

        final String harvesterUuid = "harvesterUuid";
        dataManager.setHarvestedExt(metadataId, harvesterUuid);
        metadata = metadataRepository.findOne(metadataId);
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());


        final String newSource = "newSource";
        // check that another update doesn't break the last setting
        // there used to a bug where this was the case because entity manager wasn't being flushed
        metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setSourceId(newSource);
            }
        });

        assertEquals(newSource, metadata.getSourceInfo().getSourceId());
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());

        final String harvesterUuid2 = "harvesterUuid2";
        final String harvesterUri = "harvesterUri";
        dataManager.setHarvestedExt(metadataId, harvesterUuid2, Optional.of(harvesterUri));
        metadata = metadataRepository.findOne(metadataId);
        assertEquals(harvesterUuid2, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertEquals(harvesterUri, metadata.getHarvestInfo().getUri());

        dataManager.setHarvestedExt(metadataId, null);
        metadata = metadataRepository.findOne(metadataId);
        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());
    }

    static int importMetadata(AbstractCoreIntegrationTest test, ServiceContext serviceContext) throws Exception {
        final Element sampleMetadataXml = test.getSampleMetadataXml();
        final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        return test.importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
            ReservedGroup.all.getId(), Params.GENERATE_UUID);
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        int count = (int) _metadataRepository.count();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final String mdId = _dataManager.insertMetadata(serviceContext, "iso19139", new Element("MD_Metadata"), "uuid",
            userSession.getUserIdAsInt(),
            "" + ReservedGroup.all.getId(), "sourceid", "n", "doctype", null, new ISODate().getDateAndTime(), new ISODate().getDateAndTime(),
            false, false);

        assertEquals(count + 1, _metadataRepository.count());

        metadataManager.deleteMetadata(serviceContext, mdId);

        assertEquals(count, _metadataRepository.count());
    }

    @Test
    public void testBuildPrivilegesMetadataInfo() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final Element sampleMetadataXml = getSampleMetadataXml();
        String schema = _dataManager.autodetectSchema(sampleMetadataXml);

        final String mdId1 = _dataManager.insertMetadata(serviceContext, schema, new Element(sampleMetadataXml.getName(),
                sampleMetadataXml.getNamespace()), "uuid", userSession.getUserIdAsInt(), "" + ReservedGroup.all.getId(),
            "sourceid", "n", "doctype", null, new ISODate().getDateAndTime(), new ISODate().getDateAndTime(),
            false, false);


        Element info = new Element("info", Geonet.Namespaces.GEONET);
        Map<String, Element> map = Maps.newHashMap();
        map.put(mdId1, info);
        info.removeContent();
        _dataManager.buildPrivilegesMetadataInfo(serviceContext, map);
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
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final User principal = serviceContext.getUserSession().getPrincipal();

        final GroupRepository bean = serviceContext.getBean(GroupRepository.class);
        final IMetadataManager metadataManager = serviceContext.getBean(IMetadataManager.class);
        Group group = bean.findAll().get(0);

        MetadataCategory category = serviceContext.getBean(MetadataCategoryRepository.class).findAll().get(0);

        final SourceRepository sourceRepository = serviceContext.getBean(SourceRepository.class);
        Source source = sourceRepository.save(new Source().setType(SourceType.portal).setName("GN").setUuid("sourceuuid"));

        final Element sampleMetadataXml = super.getSampleMetadataXml();
        final AbstractMetadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml)
            .setUuid(UUID.randomUUID().toString());
        metadata.getCategories().add(category);
        metadata.getDataInfo().setSchemaId("iso19139");
        metadata.getSourceInfo().setSourceId(source.getUuid()).setOwner(1);

        final AbstractMetadata templateMd = metadataManager.save(metadata);
        final String newMetadataId = _dataManager.createMetadata(serviceContext, "" + metadata.getId(), "" + group.getId(), source.getUuid(),
            principal.getId(), templateMd.getUuid(), MetadataType.METADATA.codeString, true);

        AbstractMetadata newMetadata = _metadataRepository.findOne(newMetadataId);
        assertEquals(1, newMetadata.getCategories().size());
        assertEquals(category, newMetadata.getCategories().iterator().next());
        assertEqualsText(metadata.getUuid(), newMetadata.getXmlData(false), "gmd:parentIdentifier/gco:CharacterString");

    }

    @Test
    public void testSetStatus() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final int metadataId = importMetadata(this, serviceContext);

        final MetadataStatus status = _dataManager.getStatus(metadataId);

        assertEquals(null, status);

        final ISODate changeDate = new ISODate();
        final String changeMessage = "Set to draft";
        _dataManager.setStatus(serviceContext, metadataId, 0, changeDate, changeMessage);

        final MetadataStatus loadedStatus = _dataManager.getStatus(metadataId);

        assertEquals(changeDate, loadedStatus.getId().getChangeDate());
        assertEquals(changeMessage, loadedStatus.getChangeMessage());
        assertEquals(0, loadedStatus.getStatusValue().getId());
        assertEquals(metadataId, loadedStatus.getId().getMetadataId());
        assertEquals(0, loadedStatus.getId().getStatusId());
        assertEquals(serviceContext.getUserSession().getUserIdAsInt(), loadedStatus.getId().getUserId());
    }

    @Test
    public void testSetHarvesterData() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final int metadataId = importMetadata(this, serviceContext);

        doSetHarvesterDataTest(_metadataRepository, _dataManager, metadataId);
    }

    @Test
    public void testDeleteBatchMetadata() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final SearchManager searchManager = context.getBean(SearchManager.class);
        final long startMdCount = _metadataRepository.count();
        final String lang = "eng";
        final int startIndexDocs = numDocs(searchManager, lang);

        int md1 = importMetadata(this, context);
        final int numDocsPerMd = numDocs(searchManager, lang) - startIndexDocs;
        int md2 = importMetadata(this, context);


        assertEquals(startIndexDocs + (2 * numDocsPerMd), numDocs(searchManager, lang));
        assertEquals(startMdCount + 2, _metadataRepository.count());

        Specification<Metadata> spec = where((Specification<Metadata>)MetadataSpecs.hasMetadataId(md1)).or((Specification<Metadata>)MetadataSpecs.hasMetadataId(md2));
        _dataManager.batchDeleteMetadataAndUpdateIndex(spec);

        assertEquals(startMdCount, _metadataRepository.count());

        assertEquals(startIndexDocs, numDocs(searchManager, lang));
    }

    @Test
    public void testUpdateFixedInfo() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String uuid = UUID.randomUUID().toString();
        String parentUuid = UUID.randomUUID().toString();
        Element md = Xml.loadFile(AbstractCoreIntegrationTest.class.getResource("kernel/multilingual-metadata.xml"));
        final Element updateMd = _dataManager.updateFixedInfo("iso19139", Optional.<Integer>absent(), uuid, md, parentUuid,
            UpdateDatestamp.YES, context);

        final List<Namespace> namespaces = _dataManager.getSchema("iso19139").getNamespaces();
        assertEquals(uuid, Xml.selectString(updateMd, "gmd:fileIdentifier/gco:CharacterString", namespaces));
        assertEquals(parentUuid, Xml.selectString(updateMd, "gmd:parentIdentifier/gco:CharacterString", namespaces));
        assertEquals(0, Xml.selectNodes(updateMd, "*//node()[string-length(@locale) > 3]").size());
        assertEquals(0, Xml.selectNodes(updateMd, "*//gmd:PT_Locale[string-length(@id) > 2]").size());
    }

    private int numDocs(SearchManager searchManager, String lang) throws IOException, InterruptedException {
        IndexAndTaxonomy indexReader = searchManager.getNewIndexReader(lang);
        final int startIndexDocs = indexReader.indexReader.numDocs();
        indexReader.close();
        return startIndexDocs;
    }

}
