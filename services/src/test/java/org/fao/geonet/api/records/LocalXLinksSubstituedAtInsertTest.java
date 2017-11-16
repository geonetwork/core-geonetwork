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

package org.fao.geonet.api.records;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static org.fao.geonet.domain.MetadataType.METADATA;
import static org.fao.geonet.domain.MetadataType.SUB_TEMPLATE;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.CONTACT;
import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.EXTENT;
import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.FORMAT;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;

public class LocalXLinksSubstituedAtInsertTest extends AbstractServiceIntegrationTest {
    private static final int TEST_OWNER = 42;

    private static final String CONTACT_RESOURCE = "kernel/vicinityContact.xml";
    private static final String CONTACT_RESOURCE_MULTILINGUAL = "kernel/vicinityContactMultilingual.xml";
    private static final String EXTENT_RESOURCE = "kernel/vicinityExtent.xml";
    private static final String FORMAT_RESOURCE = "kernel/vicinityFormat.xml";
    private static final String MAP_RESOURCE = "kernel/vicinityMapFlat.xml";

    private static String TEMPLATES_TO_OPERATE_ON = FORMAT + ";" + EXTENT + ";" + CONTACT;

    @Autowired
    private SpringLocalServiceInvoker invoker;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private SettingManager settingManager;

    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
        settingManager.setValue(Settings.SYSTEM_XLINK_TEMPLATES_TO_OPERATE_ON_AT_INSERT, TEMPLATES_TO_OPERATE_ON);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void insertMetadataHasToReplaceContactExtentAndFormatByXlink() throws Exception {
        Metadata decoyExtent = insertSubtemplate(EXTENT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                        .setText("-61.79842"));
        Metadata decoyContact = insertSubtemplate(CONTACT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString")
                        .setText("babar@csc.org"));
        Metadata decoyFormat = insertSubtemplate(FORMAT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:version/gco:CharacterString")
                        .setText("42"));
        Metadata format = insertSubtemplate(FORMAT_RESOURCE);
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE);

        String vicinityMapUuid = insertVicinityMap();

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
        assertVicinityMapXLinkTo(format, vicinityMapUuid);
    }


    @Test
    public void multilingualContact() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:individualName/gco:CharacterString")
                    .setText("McDuck");
            Xml.selectElement(element,
                    ".//gmd:electronicMailAddress/gco:CharacterString")
                    .setText("McDuck@csc.org");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void contactAndIndividualNameWithSpaces() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:individualName/gco:CharacterString")
                .setText("jean regis"));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:individualName/gco:CharacterString")
                .setText("jean regis"));

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void formatWithSpaces() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||format-found no match for query: +_isTemplate:s +any:shapefile +any:\"grass version 6.1\"");
        insertSubtemplate(CONTACT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        insertSubtemplate(FORMAT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:version/gco:CharacterString")
                .setText("Grass Version 6.7"));

        insertVicinityMap();
    }

    @Test
    public void contactAndOrgWithSpaces() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||contact-found no match for query: +_isTemplate:s +individual_name:babar +orgName:générale d'électricité +email:info@csc.org");
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("générale des eaux"));

        insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("générale d'électricité"));

    }

    @Test
    public void insertMetadataCanReplaceExtentButMissContactAndFormat() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||format-found no match for query: +_isTemplate:s +any:shapefile +any:\"grass version 6.1\"" + "" +
                        "||contact-found no match for query: +_isTemplate:s +individual_name:babar +orgName:csc +email:info@csc.org");

        URL extentResource = AbstractCoreIntegrationTest.class.getResource(EXTENT_RESOURCE);
        Element subtemplateElement = Xml.loadStream(extentResource.openStream());
        Metadata decoyExtentInsertedAsMetadata  = insertTemplateResourceInDb(subtemplateElement, METADATA);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void insertMetadataCantReplaceExtentNoMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||extent-found no match for query: +_isTemplate:s +_title:-61.798, 55.855, -21.371, 51.0");
        Metadata format = insertSubtemplate(FORMAT_RESOURCE);
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                        .setText("-61.79842"));

        insertVicinityMap();
    }

    @Test
    public void insertMetadataCantReplaceContactNoMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||contact-found no match for query: +_isTemplate:s +individual_name:babar +orgName:csc +email:info@csc.org");
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:individualName/gco:CharacterString")
                        .setText("totor"));
        Metadata format = insertSubtemplate(FORMAT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void insertMetadataHasToReplaceExtentAndFormatByXlinkWhenNoContact() throws Exception {
        Metadata format = insertSubtemplate(FORMAT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE);

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:pointOfContact")
                .removeContent());

        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
        assertVicinityMapXLinkTo(format, vicinityMapUuid);
    }

    @Test
    public void insertMetadataCantReplaceContactWhenToManyMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||contact-found too many matches for query: +_isTemplate:s +individual_name:babar +orgName:csc +email:info@csc.org");
        Metadata contact = insertSubtemplate(CONTACT_RESOURCE);
        Metadata contactClone = insertSubtemplate(CONTACT_RESOURCE);
        Metadata format = insertSubtemplate(FORMAT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void forExtentDescriptionTakePrecedenceOverGeom() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(CONTACT_RESOURCE);
        Metadata decoyExtent = insertSubtemplate(EXTENT_RESOURCE);
        Metadata extent = insertSubtemplate(EXTENT_RESOURCE, element -> {
            Xml.selectElement(element,
                    "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                    .setText("-61.79842");
            element.addContent(new Element("description", GMD).addContent(
                    new Element("CharacterString", GCO).setText("description avec des espaces")));
        });

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:extent").addContent(new Element("description", GMD).addContent(
                new Element("CharacterString", GCO).setText("description avec des espaces"))));

        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
    }

    private String insertVicinityMap(TestFileReworker reworker) throws Exception {
        URL vicinityMapResource = AbstractCoreIntegrationTest.class.getResource(MAP_RESOURCE);
        Element vicinityMapElement = Xml.loadStream(vicinityMapResource.openStream());
        reworker.rework(vicinityMapElement);
        String mapUuid = Xml.selectElement(vicinityMapElement, ".//gmd:fileIdentifier/gco:CharacterString").getText();

        createServiceContext();
        User user = new User().setId(TEST_OWNER);
        HttpSession session = loginAs(user);

        MockHttpServletRequest request = new MockHttpServletRequest(session.getServletContext());
        request.setRequestURI("/api/0.1/records");
        request.setContent(Xml.getString(vicinityMapElement).getBytes());
        request.setMethod("PUT");
        request.setContentType("application/xml");
        request.setCharacterEncoding("UTF-8");
        request.setSession(session);
        request.setParameter("schema", "iso19139");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SimpleMetadataProcessingReport report = (SimpleMetadataProcessingReport)invoker.invoke(request, response);

        return mapUuid;
    }
    private String insertVicinityMap() throws Exception {
        return insertVicinityMap(new NoRework());
    }

    private Metadata insertSubtemplate(String resourceName, TestFileReworker reworker) throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource(resourceName);
        Element subtemplateElement = Xml.loadStream(contactResource.openStream());
        reworker.rework(subtemplateElement);
        return insertTemplateResourceInDb(subtemplateElement, SUB_TEMPLATE);
    }

    private Metadata insertSubtemplate(String resourceName) throws Exception {
        return insertSubtemplate(resourceName, new NoRework());
    }

    interface TestFileReworker {
        void rework(Element element) throws JDOMException;
    }

    class NoRework implements TestFileReworker {
        @Override
        public void rework(Element element) {}
    }

    private Metadata insertTemplateResourceInDb(Element element, MetadataType type) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata()
                .setDataAndFixCR(element)
                .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
                .setRoot(element.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(element))
                .setType(type)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(TEST_OWNER)
                .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
                .setHarvested(false);

        Metadata dbInsertedMetadata = dataManager.insertMetadata(
                context,
                metadata,
                element,
                false,
                true,
                false,
                NO,
                false,
                false);
        return dbInsertedMetadata;
    }

    private void assertVicinityMapXLinkTo(Metadata contactMetadata, String vicinityMapUuid) throws Exception {
        MetaSearcher referencingContactSearcher = dataManager.searcherForReferencingMetadata(context, contactMetadata);
        Map<Integer, Metadata> result = ((LuceneSearcher) referencingContactSearcher).getAllMdInfo(context, 1);
        assertEquals(vicinityMapUuid, result.values().iterator().next().getUuid());
    }
}
