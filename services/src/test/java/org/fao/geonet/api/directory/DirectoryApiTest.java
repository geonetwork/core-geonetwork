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

package org.fao.geonet.api.directory;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.registries.CollectResults;
import org.fao.geonet.api.registries.DirectoryUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
public class DirectoryApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private MetadataRepository _metadataRepo;

    @Test
    public void testCollectContactEntries() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String xpath = ".//gmd:CI_ResponsibleParty";

        // Test record contains 3 contacts matching XPath
        // with 1 in 2 locations.
        final URL resource = DirectoryApiTest.class.getResource("record.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
            MetadataType.METADATA,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);

        final Metadata record = _metadataRepo.findOne(id);
        CollectResults collectResults =
            DirectoryUtils.collectEntries(context, record, xpath, null);

        assertEquals("3 contacts extracted",
            3, collectResults.getEntries().size());
    }

    @Test
    public void testCollectUniqueByEmailContactEntries() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String xpath = ".//gmd:CI_ResponsibleParty";

        // Test record contains 3 contacts matching XPath
        // with 1 in 2 locations.
        final URL resource = DirectoryApiTest.class.getResource("record.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
            MetadataType.METADATA,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);
        final Metadata record = _metadataRepo.findOne(id);
        final String uuidXpath = ".//gmd:electronicMailAddress/gco:CharacterString/text()";
        CollectResults collectResults =
            DirectoryUtils.collectEntries(context, record, xpath, uuidXpath);

        assertEquals("2 unique contacts extracted",
            2, collectResults.getEntries().size());
    }

    @Test
    public void testCollectGraphicOverviewEntries() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String xpath = ".//gmd:graphicOverview[" +
            "ends-with(*/gmd:fileName/gco:CharacterString, '.gif')]";

        // Test record contains 3 contacts matching XPath
        // with 1 in 2 locations.
        final URL resource = DirectoryApiTest.class.getResource("record.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
            MetadataType.METADATA,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);

        final Metadata record = _metadataRepo.findOne(id);
        CollectResults collectResults =
            DirectoryUtils.collectEntries(context, record, xpath, null);

        assertEquals("1 graphic overview extracted",
            1, collectResults.getEntries().size());
    }


    @Test
    public void testSynchronizeEntries() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String xpath = ".//gmd:CI_ResponsibleParty";
        List<String> propertiesToCopy = new ArrayList<>();
        propertiesToCopy.add("./gmd:role/*/@codeListValue");

        // Test record contains 3 contacts matching XPath
        // with 1 in 2 locations.
        final URL resource = DirectoryApiTest.class.getResource("record.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
            MetadataType.METADATA,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);
        final URL contact1 = DirectoryApiTest.class.getResource("contact1.xml");
        int contact1id = importMetadataXML(context, "uuid", contact1.openStream(),
            MetadataType.SUB_TEMPLATE,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);

        final Metadata record = _metadataRepo.findOne(id);
        final Metadata contact1record = _metadataRepo.findOne(contact1id);
        final String uuidXpath = ".//gmd:electronicMailAddress/gco:CharacterString/text()";
        final String contactCityFieldXpath = ".//gmd:city/gco:CharacterString/text()";
        final String contactRoleFieldXpath = "./gmd:role/*/@codeListValue";

        Element subtemplate = contact1record.getXmlData(false);
        final String subtemplateEmail = Xml.selectString(
            subtemplate, uuidXpath);
        final String subtemplateRole = Xml.selectString(
            subtemplate, contactRoleFieldXpath);
        final String subtemplateCity = Xml.selectString(
            subtemplate, contactCityFieldXpath);

        final String recordCityXpathField = ".//gmd:CI_ResponsibleParty[.//gmd:electronicMailAddress/gco:CharacterString/text() = '" +
            subtemplateEmail + "']/" + contactCityFieldXpath;
        final String recordRoleXpathField = ".//gmd:CI_ResponsibleParty[.//gmd:electronicMailAddress/gco:CharacterString/text() = '" +
            subtemplateEmail + "']/" + contactRoleFieldXpath;

        String recordCity = Xml.selectString(record.getXmlData(false), recordCityXpathField);
        assertEquals("Contact city in record is Rome",
            recordCity, "Rome"
        );


        CollectResults collectResults =
            DirectoryUtils.synchronizeEntries(context, record,
                xpath, uuidXpath, propertiesToCopy,
                false, null);

        Element updateRecord = collectResults.getUpdatedRecord();
        recordCity = Xml.selectString(
            updateRecord,
            recordCityXpathField);
        assertEquals("Contact city in subtemplate is Milano",
            "Milano", subtemplateCity
        );
        assertEquals("Contact city in record is same as in subtemplate",
            "Milano", recordCity
        );

        String recordRole = Xml.selectString(
            updateRecord,
            recordRoleXpathField);
        assertEquals("Contact role in template is shouldBePreserved",
            "pointOfContact", subtemplateRole
        );
        assertEquals("Contact role in record is shouldBePreserved",
            "shouldBePreserved", recordRole
        );
    }


    @Test
    public void testSynchronizeEntriesAndUseXlink() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String xpath = ".//gmd:CI_ResponsibleParty";
        List<String> propertiesToCopy = new ArrayList<>();
        propertiesToCopy.add("./gmd:role/*/@codeListValue");

        // Test record contains 3 contacts matching XPath
        // with 1 in 2 locations.
        final URL resource = DirectoryApiTest.class.getResource("record.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
            MetadataType.METADATA,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);
        final URL contact1 = DirectoryApiTest.class.getResource("contact1.xml");
        int contact1id = importMetadataXML(context, "uuid", contact1.openStream(),
            MetadataType.SUB_TEMPLATE,
            ReservedGroup.all.getId(),
            Params.GENERATE_UUID);
        String contact1uuid = _metadataRepo.findOne(contact1id).getUuid();

        final Metadata record = _metadataRepo.findOne(id);
        final String uuidXpath = ".//gmd:electronicMailAddress/gco:CharacterString/text()";


        CollectResults collectResults =
            DirectoryUtils.synchronizeEntries(context, record,
                xpath, uuidXpath, propertiesToCopy,
                true, null);

        Element updateRecord = collectResults.getUpdatedRecord();

        List<Namespace> ns = new ArrayList<>();
        ns.add(Geonet.Namespaces.XLINK);
        ns.add(Geonet.Namespaces.GMD);
        List contacts = Xml.selectNodes(
            updateRecord, ".//*[name() = 'gmd:pointOfContact' or name() = 'gmd:contact'][@xlink:href]", ns);
        assertEquals("2 contacts replaced by a matching subtemplate",
            2, contacts.size()
        );
        for (Object c : contacts) {
            if (c instanceof Element) {
                Element e = (Element) c;
                Attribute xlink = e.getAttribute("href", Geonet.Namespaces.XLINK);
                assertNotNull("Contact has XLink",
                    xlink
                );
                // First contact role is shouldBePreserved
                String role = e.getName().equals("contact") ?
                    "shouldBePreserved" : "pointOfContact";
                assertEquals("XLink is correct",
                    "local://eng/subtemplate?uuid=" + contact1uuid +
                        "&amp;process=./gmd:role/*/@codeListValue~" + role, xlink.getValue()
                );
            }
        }
    }
}
