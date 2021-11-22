/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import static org.fao.geonet.domain.MetadataType.SUB_TEMPLATE;
import static org.fao.geonet.domain.MetadataType.TEMPLATE;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@Ignore
public class LocalXLinksUpdateDeleteTest extends AbstractIntegrationTestWithMockedSingletons {

    private static final int TEST_OWNER = 42;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private SettingManager settingManager;

    /** Service context for test */
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
    }

    @Test
    public void updateHasToRegisterReferrersForIndexation() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        AbstractMetadata contactMetadata = insertContact(contactElement);
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);

        // TODOES
//        Object document = searchForMetadataTagged("babar");
//        assertFalse(context.getBean(IndexingList.class).getIdentifiers().contains(vicinityMapMetadata.getId()));

        Xml.selectElement(contactElement, "gmd:individualName/gco:CharacterString", Arrays.asList(GMD, GCO)).setText("momo");
        metadataManager.updateMetadata(context,
            Integer.toString(contactMetadata.getId()),
            contactElement,
            false,
            false,
            true,
            null,
            null,
            false);

//     TODOES   assertEquals(vicinityMapMetadata.getUuid(), document.getField("_uuid").stringValue());
//        assertTrue(context.getBean(IndexingList.class).getIdentifiers().contains(vicinityMapMetadata.getId()));
    }

    @Test
    public void deleteAllowedWhenRefNotExists() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);

        metadataManager.deleteMetadata(context, Integer.toString(vicinityMapMetadata.getId()));
        metadataManager.deleteMetadata(context, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    @Test
    public void deleteHasToBeForbiddenWhenRefExistsAndSettingsSaySo() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        insertVicinityMap(contactMetadata);

        try {
            metadataManager.deleteMetadata(context,
                Integer.toString(contactMetadata.getId()));
        } catch (Exception e) {

        }
        assertNotNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    @Test
    public void deleteHasToBeAllowedWhenRefExistsAndSettingsSaySo() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, true);
        AbstractMetadata contactMetadata = insertContact();
        insertVicinityMap(contactMetadata);

        metadataManager.deleteMetadata(context, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    private AbstractMetadata insertTemplateResourceInDb(Element element, MetadataType type) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(element)
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
                true,
            false,
            NO,
            false,
            false);

        return dbInsertedMetadata;
    }

    private AbstractMetadata insertVicinityMap(AbstractMetadata contactMetadata) throws Exception {
        URL vicinityMapResource = AbstractCoreIntegrationTest.class.getResource("kernel/vicinityMap.xml");
        Element vicinityMapElement = Xml.loadStream(vicinityMapResource.openStream());
        Attribute href = (Attribute) Xml.selectElement(vicinityMapElement, "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact").getAttributes().get(0);
        href.setValue(href.getValue().replace("@contact_uuid@", contactMetadata.getUuid()));
        return insertTemplateResourceInDb(vicinityMapElement, TEMPLATE);
    }

    private AbstractMetadata insertContact() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        return insertContact(contactElement);
    }

    private AbstractMetadata insertContact(Element contactElement) throws Exception {
        AbstractMetadata contactMetadata = insertTemplateResourceInDb(contactElement, SUB_TEMPLATE);

        SpringLocalServiceInvoker mockInvoker = resetAndGetMockInvoker();
        when(mockInvoker.invoke(any(String.class))).thenReturn(contactElement);
        return contactMetadata;
    }
}
