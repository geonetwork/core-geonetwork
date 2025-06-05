/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.common.collect.Maps;

import jeeves.server.context.ServiceContext;

public class XmlSerializerIntegrationTest extends AbstractCoreIntegrationTest {
    private static final String XPATH_WITHHELD = "*//*[@gco:nilReason = 'withheld']";
    private static final String XPATH_DOWNLOAD = "*//gmd:onLine[*/gmd:protocol/gco:CharacterString = 'WWW:DOWNLOAD-1.0-http--download']";
    private static final String XPATH_DYNAMIC = "*//gmd:onLine[starts-with(*/gmd:protocol/gco:CharacterString, 'OGC:WMS')]";
    private static final List<Namespace> XML_SELECT_NAMESPACE =
        Arrays.asList(
            Geonet.Namespaces.GCO,
            Geonet.Namespaces.GMD);
    final Metadata metadata = new Metadata();
    @Autowired
    XmlSerializer _xmlSerializer;
    @Autowired
    DataManager _dataManager;
    @Autowired
    IMetadataManager metadataManager;
    @Autowired
    ConfigurableApplicationContext applicationContext;
    private int _mdId;

    {
        InputStream in = XmlSerializerIntegrationTest.class.getResourceAsStream("valid-metadata.iso19139.xml");
        try {
            String data = IOUtils.toString(in);
            metadata.setData(data)
                .setUuid("uuid");

            metadata.getDataInfo()
                .setSchemaId("iso19139");

            metadata.getSourceInfo()
                .setSourceId("sourceid")
                .setOwner(1);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void setSchemaFilters(boolean withHeld, boolean keepMarkedElement) {
        MetadataSchema mds = _dataManager.getSchema(metadata.getDataInfo().getSchemaId());
        Map<String, MetadataSchemaOperationFilter> filters = new HashMap<>();
        if (withHeld) {
            if (keepMarkedElement) {
                Element mark = new Element("keepMarkedElement");
                mark.setAttribute("nilReason", "withheld", Geonet.Namespaces.GCO);

                MetadataSchemaOperationFilter editFilter = new MetadataSchemaOperationFilter(XPATH_WITHHELD, "", "editing", mark);

                filters.put(editFilter.getIfNotOperation(),
                    editFilter);
            } else {
                MetadataSchemaOperationFilter editFilter = new MetadataSchemaOperationFilter(XPATH_WITHHELD, "", "editing", null);

                filters.put(editFilter.getIfNotOperation(),
                    editFilter);
            }
        }

        MetadataSchemaOperationFilter downloadFilter = new MetadataSchemaOperationFilter(XPATH_DOWNLOAD, "", "download", null);
        filters.put(downloadFilter.getIfNotOperation(),
            downloadFilter);

        MetadataSchemaOperationFilter dynamicFilter = new MetadataSchemaOperationFilter(XPATH_DYNAMIC, "", "dynamic", null);
        filters.put(dynamicFilter.getIfNotOperation(),
            dynamicFilter);

        mds.setOperationFilters(filters);
    }

    @Before
    public void addMetadata() {
        setSchemaFilters(true, true);
        this._mdId = metadataManager.save(metadata).getId();
    }

    @Test
    public void testInternalSelectHidingWithheldSettingsDisabled() throws Exception {
        setSchemaFilters(false, true);
        assertHiddenElements(false, false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInternalSelectHidingWithheldNullServiceContext() throws Exception {
        setSchemaFilters(true, true);
        Field field = ServiceContext.class.getDeclaredField("THREAD_LOCAL_INSTANCE");
        field.setAccessible(true);
        InheritableThreadLocal<ServiceContext> threadLocalInstance = (InheritableThreadLocal<ServiceContext>) field.get(null);
        threadLocalInstance.set(null);
        assertHiddenElements(true);
    }

    @Test
    public void testInternalSelectHidingWithheldAdministrator() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(true, false, false)) {
            assertHiddenElements(false);
        }
    }

    @Test
    public void testInternalSelectHidingWithheldNotLoggedIn() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(false, false, false)) {
            assertHiddenElements(true);
        }
    }

    @Test
    public void testInternalSelectHidingDownloadAndDynamicNotLoggedIn() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(false, false, false)) {
            assertDownloadElements(false);
            assertDynamicElements(false);
        }
    }

    @Test
    public void testInternalSelectCanDownloadAndDynamicNotLoggedIn() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(false, true, true)) {
            assertDownloadElements(true);
            assertDynamicElements(true);
        }
    }

    @Test
    public void testInternalCompleteHidingHiddenElement() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(false, false, false)) {
            Element loadedMetadata = _xmlSerializer.internalSelect("1", false, true);
            List<?> withheld = Xml.selectNodes(loadedMetadata, "*//*[@gco:nilReason = 'withheld']", Arrays.asList(Geonet.Namespaces.GCO));

            assertEquals(0, withheld.size());
        }
    }

    @Test
    public void testInternalSelectHidingWithheldNotOwner() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(false, false, false)) {
            assertHiddenElements(true);
        }
    }

    @Test
    public void testInternalSelectHidingWithheldOwner() throws Exception {
        try (Closeable ignored = configureXmlSerializerAndServiceContext(true, false, false)) {
            assertHiddenElements(false);
        }
    }

    private Closeable configureXmlSerializerAndServiceContext(boolean canEdit,
                                                              boolean canDownload,
                                                              boolean canDynamic) throws Exception {//boolean isAdmin, String userId) {
        AccessManager accessManager = mock(AccessManager.class);

        when(accessManager.canEdit(any(ServiceContext.class), anyString()))
            .thenReturn(canEdit);
        when(accessManager.canDownload(any(ServiceContext.class), anyString()))
            .thenReturn(canDownload);
        when(accessManager.canDynamic(any(ServiceContext.class), anyString()))
            .thenReturn(canDynamic);

        GeonetContext gc = mock(GeonetContext.class);
        when(gc.getBean(AccessManager.class)).thenReturn(accessManager);
        final HashMap<String, Object> contexts = Maps.newHashMap();
        contexts.put(Geonet.CONTEXT_NAME, gc);
        ServiceContext context = new ServiceContext("test", applicationContext, contexts, null);
        context.setAsThreadLocal();

        final String beanName = "AccessManager";
        final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        final BeanDefinition def = beanFactory.getBeanDefinition(beanName);
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName, accessManager);
        return () -> {
            beanFactory.registerBeanDefinition(beanName, def);
            beanFactory.destroySingleton(beanName);
        };
    }

    private void assertHiddenElements(boolean checkElementsAreHidden) throws Exception {
        assertHiddenElements(true, checkElementsAreHidden);
    }

    private void assertHiddenElements(boolean isEnabled, boolean checkElementsAreHidden) throws Exception {
        final int numberMdResolution;
        final int numberAttributes;
        if (checkElementsAreHidden) {
            numberMdResolution = 0;
            numberAttributes = 1;
        } else {
            numberMdResolution = 1;
            numberAttributes = 2;
        }

        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false, true);
        List<?> resolutionElem = Xml.selectNodes(loadedMetadata,
            "*//gmd:MD_Resolution",
            XML_SELECT_NAMESPACE);
        assertEquals(numberMdResolution, resolutionElem.size());

        @SuppressWarnings("unchecked")
        List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata,
            XPATH_WITHHELD, Arrays.asList(Geonet.Namespaces.GCO));
        assertEquals(1, withheld.size());
        assertEquals(numberAttributes, withheld.get(0).getAttributes().size());
        assertEquals("withheld", withheld.get(0).getAttributeValue("nilReason", Geonet.Namespaces.GCO));

        int actualNumberOfChildElements = 0;
        for (Object element : withheld.get(0).getChildren()) {
            if (element instanceof Element) {
                actualNumberOfChildElements += 1;
            }
        }
        assertEquals(numberMdResolution, actualNumberOfChildElements);

        if (checkElementsAreHidden) {
            assertEquals("", withheld.get(0).getText());
        }
    }

    private void assertDownloadElements(boolean isEnabled) throws Exception {
        final int numberDownload = isEnabled ? 1 : 0;
        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false, true);
        @SuppressWarnings("unchecked")
        List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata,
            XPATH_DOWNLOAD,
            XML_SELECT_NAMESPACE);
        assertEquals(numberDownload, withheld.size());
    }

    private void assertDynamicElements(boolean isEnabled) throws Exception {
        final int numberDownload = isEnabled ? 1 : 0;
        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false, true);
        @SuppressWarnings("unchecked")
        List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata,
            XPATH_DYNAMIC,
            XML_SELECT_NAMESPACE);
        assertEquals(numberDownload, withheld.size());
    }
}
