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

package org.fao.geonet.component.csw;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.csw.common.Csw.NAMESPACE_CSW;
import static org.fao.geonet.csw.common.Csw.NAMESPACE_OGC;
import static org.fao.geonet.csw.common.Csw.NAMESPACE_DC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;

import jeeves.server.context.ServiceContext;

/**
 * Test Csw Transaction handling.
 * <p/>
 * User: Jesse Date: 10/17/13 Time: 7:56 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:csw-integration-test-context.xml")
public class CswTransactionIntegrationTest extends AbstractCoreIntegrationTest {
    public static final String PHOTOGRAPHIC_UUID = "46E7F9B1-99F6-3241-9039-EAE7201534F4";
    public static final String IDENTIFICATION_XPATH = "gmd:identificationInfo/*";
    public static final String TITLE_XPATH = IDENTIFICATION_XPATH + "/gmd:citation/gmd:CI_Citation/gmd:title";
    public static final String TITLE_XPATH_DE_FREE_TEXT = TITLE_XPATH + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']";
    public static final String TITLE_XPATH_FR_FREE_TEXT = TITLE_XPATH + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#FR']";
    public static final String TITLE_XPATH_EN_FREE_TEXT = TITLE_XPATH + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#EN']";
    public static final String TITLE_XPATH_CHARSTRING = TITLE_XPATH + "/gco:CharacterString";
    private static final String ABSTRACT_XPATH = IDENTIFICATION_XPATH + "/gmd:abstract";
    private static final String TOTAL_UPDATED = "totalUpdated";
    private static final String TOTAL_INSERTED = "totalInserted";
    private static final String TOTAL_DELETED = "totalDeleted";

    @Autowired
    private IMetadataManager _metadataRepository;
    @Autowired
    private IMetadataUtils _metadataUtils;
    @Autowired
    private SearchManager _searchManager;
    @Autowired
    private Transaction _transaction;
    @Autowired
    private SchemaManager _schemaManager;

    private AtomicInteger _inc = new AtomicInteger();

    public void testIllegalAccessUpdate() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());
        final ServiceContext serviceContext = createServiceContext();

        Element params = createUpdateTransaction("Title", "newTitle");
        final Element result = _transaction.execute(params, serviceContext);

        assertEquals(0, getUpdatedCount(result, TOTAL_DELETED));
        assertEquals(0, getUpdatedCount(result, TOTAL_INSERTED));
        assertEquals(0, getUpdatedCount(result, TOTAL_UPDATED));
    }

    public void testIllegalAccessInsert() throws Exception {
        final ServiceContext serviceContext = createServiceContext();

        Element metadata = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        Element params = createInsertTransactionRequest(metadata);

        final Element result = _transaction.execute(params, serviceContext);

        assertEquals(0, getUpdatedCount(result, TOTAL_DELETED));
        assertEquals(0, getUpdatedCount(result, TOTAL_INSERTED));
        assertEquals(0, getUpdatedCount(result, TOTAL_UPDATED));
    }

    public void testIllegalAccessDelete() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());
        final ServiceContext serviceContext = createServiceContext();
        Element params = createDeleteTransaction();
        final Element result = _transaction.execute(params, serviceContext);
        assertEquals(0, getUpdatedCount(result, TOTAL_DELETED));
        assertEquals(0, getUpdatedCount(result, TOTAL_INSERTED));
        assertEquals(0, getUpdatedCount(result, TOTAL_UPDATED));
    }

    @Test
    public void testInsertAsAdmin() throws Exception {
        assertEquals(0, _metadataUtils.count());

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element metadata = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        Element params = createInsertTransactionRequest(metadata);
        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_INSERTED));
        assertEquals(1, _metadataUtils.count());
        assertNotNull(_metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID));
    }

    @Test
    public void testInsertResponse() throws Exception {
        assertEquals(0, _metadataUtils.count());

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element metadata = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        Element params = createInsertTransactionRequest(metadata);
        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_INSERTED));

        final Element insertResult = response.getChild("InsertResult", NAMESPACE_CSW);
        assertNotNull("InsertResult not found", insertResult);
        final Element briefRecord = insertResult.getChild("BriefRecord", NAMESPACE_CSW);
        assertNotNull("BriefRecord not found", briefRecord);
        final Element dcIdentifier = briefRecord.getChild("identifier", NAMESPACE_DC);
        assertNotNull("dc:identifier not found", dcIdentifier);
        final Element dcTitle = briefRecord.getChild("title", NAMESPACE_DC);
        assertNotNull("dc:title not found", dcTitle);

        assertEquals("Bad identifier found", PHOTOGRAPHIC_UUID, dcIdentifier.getText());
    }

    @Test
    public void testInsertMany() throws Exception {
        assertEquals(0, _metadataUtils.count());

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element metadata = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        Element metadata2 = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        final String uuid2 = "newUUID";
        metadata2.getChild("fileIdentifier", GMD).getChild("CharacterString", GCO).setText(uuid2);

        Element params = new Element("Transaction", NAMESPACE_CSW)
            .setAttribute("service", "CSW")
            .setAttribute("version", "2.0.2")
            .addContent(new Element("Insert", NAMESPACE_CSW)
                .addContent(metadata)
                .addContent(metadata2));
        Element response = _transaction.execute(params, serviceContext);

        assertEquals(2, getUpdatedCount(response, TOTAL_INSERTED));
        assertEquals(2, _metadataUtils.count());
        assertNotNull(_metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID));
        assertNotNull(_metadataUtils.findOneByUuid(uuid2));
    }

    @Test
    public void testXPathUpdate() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final String newTitle = "NewTitle";
        Element params = createUpdateTransaction(TITLE_XPATH_CHARSTRING, newTitle);

        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));

        assertMetadataIsUpdated(TITLE_XPATH_CHARSTRING, newTitle);
    }

    @Test
    public void testFullUpdate() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        final Element metadata = Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml"));
        final String xpath = "gmd:identificationInfo/*/gmd:resourceConstraints";
        Xml.selectElement(metadata, xpath, Arrays.asList(GMD)).detach();

        assertNotNull(Xml.selectElement(_metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID).getXmlData(false), xpath,
            Arrays.asList(GMD)));

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element request = new Element("Transaction", NAMESPACE_CSW)
            .setAttribute("service", "CSW")
            .setAttribute("version", "2.0.2")
            .addContent(new Element("Update", NAMESPACE_CSW)
                .addContent(metadata));

        _transaction.execute(request, serviceContext);

        assertNull(Xml.selectElement(_metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID).getXmlData(false), xpath, Arrays.asList(GMD)));
    }

    @Test
    public void testPropertyUpdate() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final String newTitle = "NewTitle";
        Element params = createUpdateTransaction("Title", newTitle);

        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));
        assertMetadataIsUpdated(TITLE_XPATH_CHARSTRING, newTitle);

    }

    @Test
    public void testDelete() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element params = createDeleteTransaction();
        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_DELETED));

        assertNull(_metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID));
    }

    @Test
    public void testSerialUpdate() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final String newTitle = "NewTitle";
        Element params = createUpdateTransaction(TITLE_XPATH_CHARSTRING, newTitle);
        Element response = _transaction.execute(params, serviceContext);
        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));
        assertMetadataIsUpdated(TITLE_XPATH_CHARSTRING, newTitle);

        String newAbstract = "updatedAbstract";
        params = createUpdateTransaction(ABSTRACT_XPATH, newAbstract);
        response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));

        assertMetadataIsUpdated(TITLE_XPATH_CHARSTRING, newTitle);
        assertMetadataIsUpdated(ABSTRACT_XPATH, newAbstract);

    }

    @Test
    public void testXPathRemoveElement() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final String newTitle = "AddedTitle";
        Element params = createUpdateTransaction(TITLE_XPATH, newTitle);

        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));

        final AbstractMetadata updatedMetadata = _metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID);
        assertEquals(1, Xml.selectNodes(updatedMetadata.getXmlData(false), TITLE_XPATH, Arrays.asList(GCO, GMD)).size());
    }

    @Test
    public void testXPathAddXml() throws Exception {
        addPhotographicMetadataToRepository(adminUserId());

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        String charStringValue = "charStringValue";
        Element charStringEl = new Element("CharacterString", GCO).addContent(charStringValue);

        String de = "deValue";
        String fr = "frValue";
        String en = "enValue";
        Element ptFreeText = new Element("PT_FreeText", GMD)
            .addContent(new Element("textGroup", GMD).addContent(new Element("LocalisedCharacterString", GMD)
                .setAttribute("locale", "#DE").addContent(de))
            )
            .addContent(new Element("textGroup", GMD).addContent(new Element("LocalisedCharacterString", GMD)
                .setAttribute("locale", "#FR").addContent(fr))
            )
            .addContent(new Element("textGroup", GMD).addContent(new Element("LocalisedCharacterString", GMD)
                .setAttribute("locale", "#EN").addContent(en))
            );
        final Element title = new Element("title", GMD).addContent(Lists.newArrayList(charStringEl, ptFreeText));
        Element params = createUpdateTransaction(TITLE_XPATH, title);

        Element response = _transaction.execute(params, serviceContext);

        assertEquals(1, getUpdatedCount(response, TOTAL_UPDATED));

        assertMetadataIsUpdated(TITLE_XPATH_DE_FREE_TEXT, de);
        assertMetadataIsUpdated(TITLE_XPATH_EN_FREE_TEXT, en);
        assertMetadataIsUpdated(TITLE_XPATH_FR_FREE_TEXT, fr);
    }

    private void assertMetadataIsUpdated(String updateXPath, String newValue) throws IOException, JDOMException {
        final AbstractMetadata updatedMetadata = _metadataUtils.findOneByUuid(PHOTOGRAPHIC_UUID);
        assertNotNull(updatedMetadata);

        assertEqualsText(newValue, updatedMetadata.getXmlData(false), updateXPath);

    }

    private Element createDeleteTransaction() {
        Element constraint = createConstraint();
        return new Element("Transaction", NAMESPACE_CSW)
            .setAttribute("service", "CSW")
            .setAttribute("version", "2.0.2")
            .addContent(new Element("Delete", NAMESPACE_CSW)
                .addContent(constraint));
    }

    private Element createRecordProperty(String propertyName, Object value) {
        Element xml = new Element("RecordProperty", NAMESPACE_CSW)
            .addContent(new Element("Name", NAMESPACE_CSW).setText(propertyName));

        final Element valueEl = new Element("Value", NAMESPACE_CSW);
        if (value instanceof Content) {
            valueEl.addContent((Content) value);
        } else if (value instanceof String) {
            valueEl.setText((String) value);
        } else {
            throw new AssertionError("Not a supported new value : " + value);
        }

        xml.addContent(valueEl);

        return xml;
    }

    private void addPhotographicMetadataToRepository(int ownerId) throws Exception {
        AbstractMetadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata.getSourceInfo().setOwner(ownerId);
        metadata.getDataInfo().setSchemaId("iso19139");
        metadata.setUuid(PHOTOGRAPHIC_UUID);
        metadata.setDataAndFixCR(Xml.loadStream(CswTransactionIntegrationTest.class.getResourceAsStream("metadata-photographic.xml")));
        metadata = _metadataRepository.save(metadata);
        final Path schemaDir = _schemaManager.getSchemaDir("iso19139");
        List<Element> extras = Lists.newArrayList(
            SearchManager.makeField("_uuid", PHOTOGRAPHIC_UUID, false, true),
            SearchManager.makeField("_isTemplate", "n", true, true),
            SearchManager.makeField("_owner", "" + ownerId, true, true)
        );
        _searchManager.index(schemaDir, metadata.getXmlData(false), "" + metadata.getId(), extras,
            MetadataType.METADATA, metadata.getDataInfo().getRoot(), false);
    }

    private Element createUpdateTransaction(String property, Object newValue) {
        Element constraint = createConstraint();
        Element recordProperty = createRecordProperty(property, newValue);

        return new Element("Transaction", NAMESPACE_CSW)
            .setAttribute("service", "CSW")
            .setAttribute("version", "2.0.2")
            .addContent(new Element("Update", NAMESPACE_CSW)
                .addContent(constraint)
                .addContent(recordProperty));
    }

    private Element createConstraint() {
        return new Element("Constraint", NAMESPACE_CSW)
            .setAttribute("version", "1.0.0")
            .addContent(new Element("Filter", NAMESPACE_OGC)
                .addContent(new Element("PropertyIsEqualTo", NAMESPACE_OGC)
                    .addContent(new Element("PropertyName", NAMESPACE_OGC).setText("Identifier"))
                    .addContent(new Element("Literal", NAMESPACE_OGC).setText(PHOTOGRAPHIC_UUID))));
    }

    private Element createInsertTransactionRequest(Element metadata) {
        return new Element("Transaction", NAMESPACE_CSW)
            .setAttribute("service", "CSW")
            .setAttribute("version", "2.0.2")
            .addContent(new Element("Insert", NAMESPACE_CSW)
                .addContent(metadata));
    }

    private int getUpdatedCount(Element response, String type) {
        final String val = response.getChild("TransactionSummary", NAMESPACE_CSW).getChildText(type, NAMESPACE_CSW);
        return Integer.parseInt(val);
    }

    private int adminUserId() {
        return _userRepo.findAllByProfile(Profile.Administrator).get(0).getId();
    }
}
