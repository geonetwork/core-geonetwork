package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.net.URL;
import java.util.Arrays;

import static org.fao.geonet.domain.MetadataType.SUB_TEMPLATE;
import static org.fao.geonet.domain.MetadataType.TEMPLATE;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {LocalXLinksUpdateDeleteTest.TestConfig.class})
public class LocalXLinksUpdateDeleteTest extends AbstractIntegrationTestWithMockedSingletons {
    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SettingManager settingManager;

    private ServiceContext serviceContext;

    @Configuration
    static class TestConfig {
        @Bean
        public SpringLocalServiceInvoker springLocalServiceInvoker() {
            return Mockito.mock(SpringLocalServiceInvoker.class);
        }
    }

    @Before
    public void setUp() throws Exception {
        serviceContext = createServiceContext();
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
        metadataManager.updateMetadata(serviceContext,
            Integer.toString(contactMetadata.getId()),
            contactElement,
            false,
            false,
            null,
            null,
            false,
            IndexingMode.full);

//     TODOES   assertEquals(vicinityMapMetadata.getUuid(), document.getField("_uuid").stringValue());
//        assertTrue(context.getBean(IndexingList.class).getIdentifiers().contains(vicinityMapMetadata.getId()));
    }

    @Test
    public void deleteAllowedWhenRefNotExists() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);

        metadataManager.deleteMetadata(serviceContext, Integer.toString(vicinityMapMetadata.getId()));
        metadataManager.deleteMetadata(serviceContext, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    @Test
    public void deleteHasToBeForbiddenWhenRefExistsAndSettingsSaySo() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        insertVicinityMap(contactMetadata);

        try {
            metadataManager.deleteMetadata(serviceContext,
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

        metadataManager.deleteMetadata(serviceContext, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    private AbstractMetadata insertVicinityMap(AbstractMetadata contactMetadata) throws Exception {
        URL vicinityMapResource = AbstractCoreIntegrationTest.class.getResource("kernel/vicinityMap.xml");
        Element vicinityMapElement = Xml.loadStream(vicinityMapResource.openStream());
        Attribute href = (Attribute) Xml.selectElement(vicinityMapElement, "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact").getAttributes().get(0);
        href.setValue(href.getValue().replace("@contact_uuid@", contactMetadata.getUuid()));
        return insertTemplateResourceInDb(serviceContext, vicinityMapElement, TEMPLATE);
    }

    private AbstractMetadata insertContact() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        return insertContact(contactElement);
    }

    private AbstractMetadata insertContact(Element contactElement) throws Exception {
        AbstractMetadata contactMetadata = insertTemplateResourceInDb(serviceContext, contactElement, SUB_TEMPLATE);

        when(springLocalServiceInvoker.invoke(any(String.class))).thenReturn(contactElement);
        return contactMetadata;
    }
}
