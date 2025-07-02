package org.fao.geonet.kernel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.GNException;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataIndexer;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fao.geonet.domain.MetadataType.SUB_TEMPLATE;
import static org.fao.geonet.domain.MetadataType.TEMPLATE;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class LocalXLinksUpdateDeleteTest extends AbstractIntegrationTestWithMockedSingletons {
    @Autowired
    private BaseMetadataManager metadataManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private EsSearchManager searchManager;

    private ServiceContext serviceContext;

    @Autowired
    private BaseMetadataIndexer metadataIndexer;

    @Before
    public void setUp() throws Exception {
        serviceContext = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
    }

    @Test
    public void updateHasToRegisterReferrersForIndexation() throws Exception {
        Mockito.reset(metadataIndexer);

        AbstractMetadata vicinityMapMetadata = updateASubtemplateUsedByOneMetadata();

        ArgumentCaptor<List> toIndexCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataIndexer).batchIndexInThreadPool(Mockito.anyObject(), toIndexCaptor.capture());
        assertTrue(toIndexCaptor.getValue().contains(Integer.toString(vicinityMapMetadata.getId())));
    }

    @Test
    public void exceedMaxMdsReferencingSubTemplateThrowsException() {
        Mockito.reset(metadataIndexer);
        try {
            metadataManager.maxMdsReferencingSubTemplate = 0;

            assertThrows(GNException.class, () -> updateASubtemplateUsedByOneMetadata());

            Mockito.verify(metadataIndexer, never()).batchIndexInThreadPool(Mockito.anyObject(), Mockito.anyList());
        } finally {
            metadataManager.maxMdsReferencingSubTemplate = 10000;
        }
    }

    private SearchResponse countMetadataUsing(String mdUuid, String contactForResourceIndividual) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode query = objectMapper.createObjectNode()
            .set("nested", objectMapper.createObjectNode()
                .put("path", "contactForResource")
                .set("query", objectMapper.createObjectNode()
                    .set("bool", objectMapper.createObjectNode().set("must", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().set("match", objectMapper.createObjectNode().put("_id", mdUuid)))
                        .add(objectMapper.createObjectNode().set("match", objectMapper.createObjectNode().put("contactForResource.individual", contactForResourceIndividual))))))
            );

        return this.searchManager.query(query, Collections.emptySet(), 0, 10, null);
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

    private AbstractMetadata updateASubtemplateUsedByOneMetadata() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        AbstractMetadata contactMetadata = insertContact(contactElement);
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);
        assertEquals(1, countMetadataUsing(vicinityMapMetadata.getUuid(), "babar").hits().hits().size());
        assertEquals(0, countMetadataUsing(vicinityMapMetadata.getUuid(), "momo").hits().hits().size());

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
        return vicinityMapMetadata;
    }

    static public class SpyFactory {
        @Autowired
        BaseMetadataIndexer baseMetadataIndexer;

        BaseMetadataIndexer build() {
            return Mockito.spy(baseMetadataIndexer);
        }
    }
}
