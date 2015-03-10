package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlSerializerIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    XmlSerializer _xmlSerializer;
    @Autowired
    DataManager _dataManager;
    @Autowired
    MetadataRepository _metadataRepo;
    @Autowired
    ConfigurableApplicationContext applicationContext;

    private static final String OWNER_ID = "1234";
    private static final String XPATH_WITHHELD = "*//*[@gco:nilReason = 'withheld']";
    private static final String XPATH_DOWNLOAD = "*//gmd:onLine[*/gmd:protocol/gco:CharacterString = 'WWW:DOWNLOAD-1.0-http--download']";
    private static final String XPATH_DYNAMIC = "*//gmd:onLine[starts-with(*/gmd:protocol/gco:CharacterString, 'OGC:WMS')]";
    private static final List<Namespace> XML_SELECT_NAMESPACE =
            Arrays.asList(
                    Geonet.Namespaces.GCO,
                    Geonet.Namespaces.GMD);

    final Metadata metadata = new Metadata();

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

    private int _mdId;

    public void setSchemaFilters(boolean withHeld, boolean keepMarkedElement) {
        MetadataSchema mds = _dataManager.getSchema(metadata.getDataInfo().getSchemaId());
        Map<String, Pair<String, Element>> filters = new HashMap<String, Pair<String, Element>>();
        if (withHeld) {
            if (keepMarkedElement) {
                Element mark = new Element("keepMarkedElement");
                mark.setAttribute("nilReason", "withheld", Geonet.Namespaces.GCO);
                filters.put("editing",
                        Pair.read(XPATH_WITHHELD, mark));
            } else {
                filters.put("editing",
                        Pair.<String, Element>read(XPATH_WITHHELD, null));
            }
        }

        filters.put("download",
                Pair.<String, Element>read(XPATH_DOWNLOAD, null));
        filters.put("dynamic",
                Pair.<String, Element>read(XPATH_DYNAMIC, null));

        mds.setOperationFilters(filters);
    }

    @Before
    public void addMetadata() {
        setSchemaFilters(true, true);
        this._mdId = _metadataRepo.save(metadata).getId();
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
        configureXmlSerializerAndServiceContext(true, false, false);

        assertHiddenElements(false);
    }

    @Test
    public void testInternalSelectHidingWithheldNotLoggedIn() throws Exception {
        configureXmlSerializerAndServiceContext(false, false, false);

        assertHiddenElements(true);
    }

    @Test
    public void testInternalSelectHidingDownloadAndDynamicNotLoggedIn() throws Exception {
        configureXmlSerializerAndServiceContext(false, false, false);

        assertDownloadElements(false);
        assertDynamicElements(false);
    }

    @Test
    public void testInternalSelectCanDownloadAndDynamicNotLoggedIn() throws Exception {
        configureXmlSerializerAndServiceContext(false, true, true);

        assertDownloadElements(true);
        assertDynamicElements(true);
    }

    @Test
    public void testInternalCompleteHidingHiddenElement() throws Exception {
        configureXmlSerializerAndServiceContext(false, false, false);

        Element loadedMetadata = _xmlSerializer.internalSelect("1", false);
        List<?> withheld = Xml.selectNodes(loadedMetadata, "*//*[@gco:nilReason = 'withheld']", Arrays.asList(Geonet.Namespaces.GCO));

        assertEquals(0, withheld.size());
    }

    @Test
    public void testInternalSelectHidingWithheldNotOwner() throws Exception {
        configureXmlSerializerAndServiceContext(false, false, false);

        assertHiddenElements(true);
    }

    @Test
    public void testInternalSelectHidingWithheldOwner() throws Exception {
        configureXmlSerializerAndServiceContext(true, false, false);

        assertHiddenElements(false);
    }

    private ServiceContext configureXmlSerializerAndServiceContext(boolean canEdit,
                                                                   boolean canDownload,
                                                                   boolean canDynamic) throws Exception {//boolean isAdmin, String userId) {
        ServiceContext context = mock(ServiceContext.class);
        doCallRealMethod().when(context).setAsThreadLocal();

        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.canEdit(any(ServiceContext.class), anyString()))
                .thenReturn(canEdit);
        when(accessManager.canDownload(any(ServiceContext.class), anyString()))
                .thenReturn(canDownload);
        when(accessManager.canDynamic(any(ServiceContext.class), anyString()))
                .thenReturn(canDynamic);
        GeonetContext gc = mock(GeonetContext.class);
        when(gc.getBean(AccessManager.class)).thenReturn(accessManager);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(gc);

        context.setAsThreadLocal();

        _xmlSerializer.accessManager = accessManager;
        return context;
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

        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false);
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
        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false);
        @SuppressWarnings("unchecked")
        List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata,
                XPATH_DOWNLOAD,
                XML_SELECT_NAMESPACE);
        assertEquals(numberDownload, withheld.size());
    }

    private void assertDynamicElements(boolean isEnabled) throws Exception {
        final int numberDownload = isEnabled ? 1 : 0;
        Element loadedMetadata = _xmlSerializer.internalSelect("" + _mdId, false);
        @SuppressWarnings("unchecked")
        List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata,
                XPATH_DYNAMIC,
                XML_SELECT_NAMESPACE);
        assertEquals(numberDownload, withheld.size());
    }
}
