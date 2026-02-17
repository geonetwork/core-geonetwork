package org.fao.geonet.kernel;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.repository.UserGroupRepository;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlSerializerTest {

    private ConfigurableApplicationContext mockContext;
    private AccessManager mockAccessManager;
    private DataManager mockDataManager;
    private UserGroupRepository mockUserGroupRepository;

    @Before
    public void setUp() {
        mockContext = mock(ConfigurableApplicationContext.class);
        mockAccessManager = mock(AccessManager.class);
        mockDataManager = mock(DataManager.class);
        mockUserGroupRepository = mock(UserGroupRepository.class);

        when(mockContext.getBean(AccessManager.class)).thenReturn(mockAccessManager);
        when(mockContext.getBean(DataManager.class)).thenReturn(mockDataManager);
        when(mockContext.getBean(UserGroupRepository.class)).thenReturn(mockUserGroupRepository);

        ApplicationContextHolder.set(mockContext);
    }

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    private XmlSerializer createXmlSerializer() {
        return new XmlSerializer() {
            @Override public void delete(String id, ServiceContext context) throws Exception {}
            @Override public void update(String id, Element xml, String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) throws Exception {}
            @Override public AbstractMetadata insert(AbstractMetadata metadata, Element dataXml, ServiceContext context) throws Exception { return null; }
            @Override public Element select(ServiceContext context, String id) throws Exception { return null; }
            @Override public Element selectNoXLinkResolver(String id, boolean isIndexingTask, boolean applyOperationsFilters) throws Exception { return null; }
        };
    }

    private ServiceContext setupServiceContextWithUserSession(UserSession mockUserSession) {
        Map<String, Object> contexts = new HashMap<>();
        ServiceContext context = new ServiceContext("test", mockContext, contexts, null);
        context.setUserSession(mockUserSession);
        context.setAsThreadLocal();
        return context;
    }

    @Test
    public void testRemoveHiddenElementsGroupOwnerFilter() throws Exception {
        XmlSerializer xmlSerializer = createXmlSerializer();

        Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        List<Namespace> namespaces = Collections.singletonList(gmd);

        AbstractMetadata metadata = mock(AbstractMetadata.class);
        MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
        MetadataSourceInfo sourceInfo = mock(MetadataSourceInfo.class);

        when(metadata.getId()).thenReturn(1);
        when(metadata.getDataInfo()).thenReturn(dataInfo);
        when(metadata.getSourceInfo()).thenReturn(sourceInfo);
        when(dataInfo.getSchemaId()).thenReturn("iso19139");
        when(sourceInfo.getGroupOwner()).thenReturn(10);

        MetadataSchema mockSchema = mock(MetadataSchema.class);
        when(mockDataManager.getSchema("iso19139")).thenReturn(mockSchema);
        when(mockSchema.getNamespaces()).thenReturn(namespaces);

        MetadataSchemaOperationFilter groupOwnerFilter = new MetadataSchemaOperationFilter("gmd:hidden", "", "groupOwner", null);
        when(mockSchema.getOperationFilter("groupOwner")).thenReturn(groupOwnerFilter);

        UserSession mockUserSession = mock(UserSession.class);
        ServiceContext realServiceContext = setupServiceContextWithUserSession(mockUserSession);

        // Case 1: User IS in the owner group
        when(mockUserGroupRepository.findGroupIds(ArgumentMatchers.<Specification<UserGroup>>any())).thenReturn(Arrays.asList(10, 20));

        Element metadataXml1 = new Element("MD_Metadata", gmd);
        Element hiddenElement1 = new Element("hidden", gmd);
        metadataXml1.addContent(hiddenElement1);
        when(metadata.getXmlData(false)).thenReturn(metadataXml1);

        Element result1 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNotNull(result1.getChild("hidden", gmd));

        // Case 2: User IS NOT in the owner group
        when(mockUserGroupRepository.findGroupIds(ArgumentMatchers.<Specification<UserGroup>>any())).thenReturn(Arrays.asList(20, 30));

        Element metadataXml2 = new Element("MD_Metadata", gmd);
        Element hiddenElement2 = new Element("hidden", gmd);
        metadataXml2.addContent(hiddenElement2);
        when(metadata.getXmlData(false)).thenReturn(metadataXml2);

        Element result2 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNull(result2.getChild("hidden", gmd));
    }

    @Test
    public void testRemoveHiddenElementsAuthenticatedFilter() throws Exception {
        XmlSerializer xmlSerializer = createXmlSerializer();

        Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        List<Namespace> namespaces = Collections.singletonList(gmd);

        AbstractMetadata metadata = mock(AbstractMetadata.class);
        MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
        MetadataSourceInfo sourceInfo = mock(MetadataSourceInfo.class);

        when(metadata.getId()).thenReturn(1);
        when(metadata.getDataInfo()).thenReturn(dataInfo);
        when(metadata.getSourceInfo()).thenReturn(sourceInfo);
        when(dataInfo.getSchemaId()).thenReturn("iso19139");

        MetadataSchema mockSchema = mock(MetadataSchema.class);
        when(mockDataManager.getSchema("iso19139")).thenReturn(mockSchema);
        when(mockSchema.getNamespaces()).thenReturn(namespaces);

        MetadataSchemaOperationFilter authenticatedFilter = new MetadataSchemaOperationFilter("gmd:authenticatedOnly", "", "authenticated", null);
        when(mockSchema.getOperationFilter("authenticated")).thenReturn(authenticatedFilter);

        UserSession mockUserSession = mock(UserSession.class);
        ServiceContext realServiceContext = setupServiceContextWithUserSession(mockUserSession);

        // Case 1: User IS authenticated
        when(mockUserSession.isAuthenticated()).thenReturn(true);

        Element metadataXml1 = new Element("MD_Metadata", gmd);
        Element authElement1 = new Element("authenticatedOnly", gmd);
        metadataXml1.addContent(authElement1);
        when(metadata.getXmlData(false)).thenReturn(metadataXml1);

        Element result1 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNotNull(result1.getChild("authenticatedOnly", gmd));

        // Case 2: User IS NOT authenticated
        when(mockUserSession.isAuthenticated()).thenReturn(false);

        Element metadataXml2 = new Element("MD_Metadata", gmd);
        Element authElement2 = new Element("authenticatedOnly", gmd);
        metadataXml2.addContent(authElement2);
        when(metadata.getXmlData(false)).thenReturn(metadataXml2);

        Element result2 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNull(result2.getChild("authenticatedOnly", gmd));
    }

    @Test
    public void testRemoveHiddenElementsDownloadFilter() throws Exception {
        XmlSerializer xmlSerializer = createXmlSerializer();

        Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        List<Namespace> namespaces = Collections.singletonList(gmd);

        AbstractMetadata metadata = mock(AbstractMetadata.class);
        MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
        MetadataSourceInfo sourceInfo = mock(MetadataSourceInfo.class);

        when(metadata.getId()).thenReturn(1);
        when(metadata.getDataInfo()).thenReturn(dataInfo);
        when(metadata.getSourceInfo()).thenReturn(sourceInfo);
        when(dataInfo.getSchemaId()).thenReturn("iso19139");

        MetadataSchema mockSchema = mock(MetadataSchema.class);
        when(mockDataManager.getSchema("iso19139")).thenReturn(mockSchema);
        when(mockSchema.getNamespaces()).thenReturn(namespaces);

        MetadataSchemaOperationFilter downloadFilter = new MetadataSchemaOperationFilter("gmd:downloadOnly", "", ReservedOperation.download.name(), null);
        when(mockSchema.getOperationFilter(ReservedOperation.download)).thenReturn(downloadFilter);

        UserSession mockUserSession = mock(UserSession.class);
        ServiceContext realServiceContext = setupServiceContextWithUserSession(mockUserSession);

        // Case 1: User can download
        when(mockAccessManager.canDownload(ArgumentMatchers.any(ServiceContext.class), ArgumentMatchers.eq("1"))).thenReturn(true);

        Element metadataXml1 = new Element("MD_Metadata", gmd);
        Element downloadElement1 = new Element("downloadOnly", gmd);
        metadataXml1.addContent(downloadElement1);
        when(metadata.getXmlData(false)).thenReturn(metadataXml1);

        Element result1 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNotNull(result1.getChild("downloadOnly", gmd));

        // Case 2: User cannot download
        when(mockAccessManager.canDownload(ArgumentMatchers.any(ServiceContext.class), ArgumentMatchers.eq("1"))).thenReturn(false);

        Element metadataXml2 = new Element("MD_Metadata", gmd);
        Element downloadElement2 = new Element("downloadOnly", gmd);
        metadataXml2.addContent(downloadElement2);
        when(metadata.getXmlData(false)).thenReturn(metadataXml2);

        Element result2 = xmlSerializer.removeHiddenElements(false, metadata, true);
        assertNull(result2.getChild("downloadOnly", gmd));
    }
}
