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

import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.User;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

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

    private static final String CONTACT_RESOURCE = "samples/vicinityContact.xml";
    private static final String CONTACT_RESOURCE_MULTILINGUAL = "samples/vicinityContactMultilingual.xml";
    private static final String EXTENT_RESOURCE = "samples/vicinityExtent.xml";
    private static final String FORMAT_RESOURCE = "samples/vicinityFormat.xml";
    private static final String MAP_RESOURCE = "samples/vicinityMapFlat.xml";

    private static String TEMPLATES_TO_OPERATE_ON = FORMAT + ";" + EXTENT + ";" + CONTACT;

    @Autowired
    private SpringLocalServiceInvoker invoker;

    @Autowired
    public EsSearchManager esSearchManager;

    @Autowired
    protected IMetadataIndexer indexer;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected BaseMetadataManager metadataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    protected MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private EsRestClient esRestClient;

    @Value("${es.index.records:gn-records}")
    private String defaultIndex = "records";

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
        settingManager.setValue(Settings.SYSTEM_XLINK_TEMPLATES_TO_OPERATE_ON_AT_INSERT, TEMPLATES_TO_OPERATE_ON);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @After
    public void resetIndex() throws Exception {
        esRestClient.deleteByQuery(defaultIndex, "valid:1");
    }

    @Test
    public void insertMetadataHasToReplaceContactExtentAndFormatByXlink() throws Exception {
        AbstractMetadata decoyExtent = insertSubtemplate(EXTENT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                        .setText("-61.79842"));
        AbstractMetadata decoyContact = insertSubtemplate(CONTACT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString")
                        .setText("babar@csc.org"));
        AbstractMetadata decoyFormat = insertSubtemplate(FORMAT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:version/gco:CharacterString")
                        .setText("42"));
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);

        String vicinityMapUuid = insertVicinityMap();

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
        assertVicinityMapXLinkTo(format, vicinityMapUuid);

        Element metadata = metadataManager.getMetadata(dataManager.getMetadataId(vicinityMapUuid));
        assertEquals(
        "local://srv/api/registries/entries/" +
                contact.getUuid() +
                "?process=gmd:role/gmd:CI_RoleCode/@codeListValue~resourceProvider&lang=eng,ger,ita,fre",
                ((Attribute)(Xml.selectElement(metadata, "*//gmd:pointOfContact").getAttributes().get(1))).getValue());

        metadataManager.getMetadata(dataManager.getMetadataId(vicinityMapUuid));
    }

    @Test
    public void invalidTemplateAreNotTakenIntoAccount() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||extent-found no match for query: +isTemplate:s +valid:1 +resourceTitle:\"-61.798, 55.855, -21.371, 51.088\" +root:\"gmd:EX_Extent\"");
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);
        extent = validate(extent, false);

        String vicinityMapUuid = insertVicinityMap();
    }

    @Test
    public void multilingualContactEn() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:individualName/gco:CharacterString")
                    .setText("mcDûCk");
            Xml.selectElement(element,
                    ".//gmd:electronicMailAddress/gco:CharacterString")
                    .setText("mcduck@csc.org");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
        Element md = dataManager.getMetadata(dataManager.getMetadataId(vicinityMapUuid));
        String href = ((Element)Xml.selectNodes(md, ".//*[@uuidref='" + contact.getUuid() + "']", new ArrayList<>(ISO19139SchemaPlugin.allNamespaces)).get(0))
                .getAttributeValue("href", Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
        assertEquals("&lang=eng,ger,ita,fre", href.substring(href.indexOf("&")));
    }
    @Test
    public void multilingualContactGer() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:individualName/gco:CharacterString")
                    .setText("dâgObert");
            Xml.selectElement(element,
                    ".//gmd:electronicMailAddress/gco:CharacterString")
                    .setText("dagobert@csc.org");
            Xml.selectElement(element,
                    ".//gmd:LanguageCode")
                    .setAttribute("codeListValue", "ger");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void templateInEngButCharacterStringInGer() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:individualName/gco:CharacterString")
                    .setText("dâgObert");
            Xml.selectElement(element,
                    ".//gmd:electronicMailAddress/gco:CharacterString")
                    .setText("dagôbErt@csc.org");
            Xml.selectElement(element,
                    ".//gmd:LanguageCode")
                    .setAttribute("codeListValue", "eng");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void noCharacterStringButKindOfFrenchTranslationAvailable() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:LanguageCode")
                    .setAttribute("codeListValue", "fre");
            replaceCharacterStringThruLocalised(element, ".//gmd:individualName", "mcducK", "#FR");
            replaceCharacterStringThruLocalised(element, ".//gmd:electronicMailAddress", "mCdùck@csc.org", "#FR");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void noCharacterStringButKindOfGermanTranslationAvailable() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:LanguageCode")
                    .setAttribute("codeListValue", "ger");
            replaceCharacterStringThruLocalised(element, ".//gmd:individualName", "Mcduck", "#DE");
            replaceCharacterStringThruLocalised(element, ".//gmd:electronicMailAddress", "mcDück@csc.org", "#DE");
        });
        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void metaDataInFrenchButKindOfGermanTranslationAvailable() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||contact-found no match for query: +isTemplate:s +valid:1 +root:\"gmd:CI_ResponsibleParty\" +contactIndividualNameObject.\\*:\"\" +contactOrgObject.\\*:\"csc\" +contactEmailObject.\\*:");
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE_MULTILINGUAL);

        String vicinityMapUuid = insertVicinityMap(element -> {
            Xml.selectElement(element,
                    ".//gmd:LanguageCode")
                    .setAttribute("codeListValue", "fre");
            replaceCharacterStringThruLocalised(element, ".//gmd:individualName", "mcduck", "#DE");
            replaceCharacterStringThruLocalised(element, ".//gmd:electronicMailAddress", "mcduck@csc.org", "#DE");
        });

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void formatWithSpaces() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||format-found no match for query: +isTemplate:s +valid:1 +formatName:\"ShapeFile\" +formatVersion:\"Grass Version 6.1\" +root:\"gmd:MD_Format\"");
        insertSubtemplate(CONTACT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        insertSubtemplate(FORMAT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:version/gco:CharacterString")
                .setText("Grass Version 6.7"));

        insertVicinityMap();
    }

    @Test
    public void insertMetadataCanReplaceExtentButMissContactAndFormat() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||format-found no match for query: +isTemplate:s +valid:1 +formatName:\"ShapeFile\" +formatVersion:\"Grass Version 6.1\" +root:\"gmd:MD_Format\"" +
                "||contact-found no match for query: +isTemplate:s +valid:1 +root:\"gmd:CI_ResponsibleParty\" +contactIndividualNameObject.\\*:\"babar\" +contactOrgObject.\\*:\"csc\" +contactEmailObject.\\*:\"info@csc.org\"");
        AbstractMetadata decoyExtentInsertedAsMetadata  = insertSubtemplate(EXTENT_RESOURCE,
                element -> Xml.selectElement(element,
                                "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                        .setText("-61.79842"));
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void insertMetadataCantReplaceExtentNoMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||extent-found no match for query: +isTemplate:s +valid:1 +resourceTitle:\"-61.798, 55.855, -21.371, 51.088\" +root:\"gmd:EX_Extent\"");
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE,
                element -> Xml.selectElement(element,
                        "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                        .setText("-61.79842"));

        insertVicinityMap();
    }

    @Test
    public void insertMetadataCantReplaceContactNoMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                        "||contact-found no match for query: +isTemplate:s +valid:1 +root:\"gmd:CI_ResponsibleParty\" +contactIndividualNameObject.\\*:\"babar\" +contactOrgObject.\\*:\"csc\" +contactEmailObject.\\*:\"info@csc.org\"");
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE,
                element -> {Xml.selectElement(element,
                        "gmd:individualName/gco:CharacterString")
                        .setText("totor");
                    });
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void insertMetadataHasToReplaceExtentAndFormatByXlinkWhenNoContact() throws Exception {
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:pointOfContact")
                .removeContent());

        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
        assertVicinityMapXLinkTo(format, vicinityMapUuid);
    }

    @Ignore
    @Test
    public void insertMetadataCantReplaceContactWhenToManyMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("||contact-found too many matches for query: ....");
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata contactClone = insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata format = insertSubtemplate(FORMAT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE);

        insertVicinityMap();
    }

    @Test
    public void forExtentDescriptionTakePrecedenceOverGeom() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata decoyExtent = insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE, element -> {
            Xml.selectElement(element,
                    "gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal")
                    .setText("-61.79842");
            element.addContent(new Element("description", GMD).addContent(
                    new Element("CharacterString", GCO).setText("descriptïon Avec des espaces")));
        });

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:extent").addContent(new Element("description", GMD).addContent(
                new Element("CharacterString", GCO).setText("description avec des Espâces"))));

        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
    }

    @Test
    public void contactOrgNameWithCaseShift() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("SwissTOpô"));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("sWissTôPo"));

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void contactOrgNameWithSpecialChar() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
            ".//gmd:organisationName/gco:CharacterString")
            .setText("Cantons [D+M]"));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("Cantons [D+M]"));


        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Ignore
    public void contactOrgNameEmpty() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText(""));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText(""));

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void contactOrgWithSpacesNoMatch() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(
                "||contact-found no match for query: +isTemplate:s +valid:1 +root:\"gmd:CI_ResponsibleParty\" +contactIndividualNameObject.\\*:\"babar\" +contactOrgObject.\\*:\"générale d'électricité\" +contactEmailObject.\\*:\"info@csc.org\"");
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
    public void contactOrgWithSpaces() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("genérale des eaux"));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:organisationName/gco:CharacterString")
                .setText("génerale des eaux"));

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);

    }

    @Test
    public void contactIndividualNameWithSpacesAndCaseShift() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(EXTENT_RESOURCE);
        AbstractMetadata contact = insertSubtemplate(CONTACT_RESOURCE, element -> Xml.selectElement(element,
                ".//gmd:/gco:CharacterString")
                .setText("jéaN regis"));

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:individualName/gco:CharacterString")
                .setText("jean Régis"));

        assertVicinityMapXLinkTo(contact, vicinityMapUuid);
    }

    @Test
    public void extentDescriptionWithCaseShift() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);
        insertSubtemplate(CONTACT_RESOURCE);
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE, element -> {
            element.addContent(new Element("description", GMD).addContent(
                    new Element("CharacterString", GCO).setText("description avec des espaces")));
        });

        String vicinityMapUuid = insertVicinityMap(element -> Xml.selectElement(element,
                ".//gmd:extent").addContent(new Element("description", GMD).addContent(
                new Element("CharacterString", GCO).setText("dEscription avec des espaces"))));

        assertVicinityMapXLinkTo(extent, vicinityMapUuid);
    }

    @Test
    public void queryForFormat() throws Exception {
        insertSubtemplate(FORMAT_RESOURCE);

        SearchResponse response = esRestClient.query(defaultIndex, "+isTemplate:s +valid:1 (any:(GML) resourceTitleObject.\\*:(Grass)^2)", null, Collections.emptySet(), 0, 10000);

        assertEquals(1, response.getHits().getTotalHits().value);
    }

    @Test
    public void queryForContact() throws Exception {
        insertSubtemplate(CONTACT_RESOURCE);

        SearchResponse response = esRestClient.query(defaultIndex, "+isTemplate:s +valid:1 (any:(baba*) resourceTitleObject.\\*:(baba*)^2)", null, Collections.emptySet(), 0, 10000);

        assertEquals(1, response.getHits().getTotalHits().value);
    }

    @Test
    public void queryForExtent() throws Exception {
        AbstractMetadata extent = insertSubtemplate(EXTENT_RESOURCE, element -> {
            element.addContent(new Element("description", GMD).addContent(
                    new Element("CharacterString", GCO).setText("Aarau")));
        });

        SearchResponse response = esRestClient.query(defaultIndex, "+isTemplate:s +valid:1 (any:(Aa*) resourceTitleObject.\\*:(Aa*)^2)", null, Collections.emptySet(), 0, 10000);

        assertEquals(1, response.getHits().getTotalHits().value);
    }

    private String insertVicinityMap(TestFileReworker reworker) throws Exception {
        URL vicinityMapResource = LocalXLinksSubstituedAtInsertTest.class.getResource(MAP_RESOURCE);
        Element vicinityMapElement = Xml.loadStream(vicinityMapResource.openStream());
        reworker.rework(vicinityMapElement);
        String mapUuid = Xml.selectElement(vicinityMapElement, ".//gmd:fileIdentifier/gco:CharacterString").getText();

        createServiceContext();
        User user = new User().setId(TEST_OWNER);
        HttpSession session = loginAs(user);

        MockHttpServletRequest request = new MockHttpServletRequest(session.getServletContext());
        request.setRequestURI("/_portal_/api/records");
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

    @Transactional
    private AbstractMetadata insertSubtemplate(String resourceName, TestFileReworker reworker) throws Exception {
        URL resource = LocalXLinksSubstituedAtInsertTest.class.getResource(resourceName);
        Element subtemplateElement = Xml.loadStream(resource.openStream());
        reworker.rework(subtemplateElement);
        AbstractMetadata metadata = insertTemplateResourceInDb(subtemplateElement, SUB_TEMPLATE);
        return validate(metadata, true);
    }

    private AbstractMetadata insertSubtemplate(String resourceName) throws Exception {
        return insertSubtemplate(resourceName, new NoRework());
    }

    interface TestFileReworker {
        void rework(Element element) throws JDOMException;
    }

    class NoRework implements TestFileReworker {
        @Override
        public void rework(Element element) {}
    }

    private AbstractMetadata insertTemplateResourceInDb(Element element, MetadataType type) throws Exception {
        loginAsAdmin(context);

        AbstractMetadata metadata= new Metadata()
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

        AbstractMetadata dbInsertedMetadata = metadataManager.insertMetadata(
                context,
                metadata,
                element,
                false,
                false,
                NO,
                false,
                false);
        return dbInsertedMetadata;
    }

    private AbstractMetadata validate(AbstractMetadata metadata, boolean isvalid) throws Exception {
        MetadataValidation metadataValidation = new MetadataValidation().
                setId(new MetadataValidationId(metadata.getId(), "subtemplate")).
                setStatus(isvalid ? MetadataValidationStatus.VALID : MetadataValidationStatus.INVALID).
                setRequired(true).
                setNumTests(0).
                setNumFailures(0);
        this.metadataValidationRepository.save(metadataValidation);
        indexer.indexMetadata(("" + metadata.getId()), true);
        return metadata;
    }

    private void assertVicinityMapXLinkTo(AbstractMetadata subtemplateMetadata, String vicinityMapUuid) throws Exception {
        SearchResponse response = esSearchManager.query("+xlink:*" + subtemplateMetadata.getUuid() + "* +isTemplate:(y OR n)", null, Collections.emptySet(), 0, 10);
        assertEquals(vicinityMapUuid, response.getHits().getAt(0).getId());
    }

    private void replaceCharacterStringThruLocalised(Element element, String fieldKey, String value, String locale) throws JDOMException {
        Element nameElem = Xml.selectElement(element,
                fieldKey);
        nameElem.removeContent();
        nameElem.addContent(new Element("PT_FreeText", GMD)
                .addContent(new Element("textGroup", GMD)
                        .addContent(new Element("LocalisedCharacterString", GMD)
                                .setText(value)
                                .setAttribute("locale", locale))));
    }
}
