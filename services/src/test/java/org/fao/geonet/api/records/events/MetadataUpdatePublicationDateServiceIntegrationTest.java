/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.events;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataUpdatePublicationDateServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MetadataUpdatePublicationDateService service;

    @Autowired
    private IMetadataUtils repository;

    @PersistenceContext
    private EntityManager entityManager;

    private ServiceContext context;
    private AbstractMetadata metadata;
    private List<Namespace> ns = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);

        ns.addAll(ISO19139SchemaPlugin.allNamespaces);

        Element sample = getSampleISO19139MetadataXml();
        metadata = injectMetadataInDb(sample, context, true);
    }

    @Test
    public void testAddPublicationDate() throws Exception {
        AbstractMetadata record = repository.findOne(metadata.getId());
        Element initialXml = record.getXmlData(false);

        // Ensure no publication date exists
        assertEquals(0, Xml.selectNodes(initialXml, "//gmd:identificationInfo/*/gmd:citation/*/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication']", ns).size());

        ISODate pubDate = new ISODate("2026-07-02T10:00:00");
        service.addPublicationDate(record, pubDate);
        entityManager.flush();
        entityManager.clear();

        AbstractMetadata updatedRecord = repository.findOne(metadata.getId());
        Element updatedXml = updatedRecord.getXmlData(false);

        assertEquals(1, Xml.selectNodes(updatedXml, "gmd:identificationInfo/*/gmd:citation/*/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication']", ns).size());
        Element dateText = (Element) Xml.selectSingle(updatedXml, "gmd:identificationInfo/*/gmd:citation/*/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication']//gco:DateTime", ns);
        assertNotNull(dateText);
        assertEquals(pubDate.toString(), dateText.getText());
    }

    @Test
    public void testRemovePublicationDate() throws Exception {
        AbstractMetadata record = repository.findOne(metadata.getId());

        // 1. Add a publication date first
        ISODate pubDate = new ISODate("2026-07-02T10:00:00");
        service.addPublicationDate(record, pubDate);
        entityManager.flush();
        entityManager.clear();

        AbstractMetadata recordWithPubDate = repository.findOne(metadata.getId());
        assertEquals(1, Xml.selectNodes(recordWithPubDate.getXmlData(false), "gmd:identificationInfo/*/gmd:citation/*/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication']", ns).size());

        // 2. Remove it
        service.removePublicationDate(recordWithPubDate);
        entityManager.flush();
        entityManager.clear();

        AbstractMetadata updatedRecord = repository.findOne(metadata.getId());
        Element updatedXml = updatedRecord.getXmlData(false);

        assertEquals(0, Xml.selectNodes(updatedXml, "gmd:identificationInfo/*/gmd:citation/*/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication']", ns).size());
    }
}
