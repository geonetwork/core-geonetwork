package org.fao.geonet.kernel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jeeves.constants.Jeeves;
import jeeves.guiservices.session.JeevesUser;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class XmlSerializerTest {
	
	public class DummyXmlSerializer extends XmlSerializer {

		public DummyXmlSerializer(SettingManager settingManager) {
			super(settingManager);
		}

		@Override
		public void delete(Dbms dbms, String table, String id,
				ServiceContext context) throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public void update(Dbms dbms, String id, Element xml,
				String changeDate, boolean updateDateStamp,
				String uuid, ServiceContext context) throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public String insert(Dbms dbms, String schema, Element xml, int serial,
				String source, String uuid, String createDate,
				String changeDate, String isTemplate, String title, int owner,
				String groupOwner, String docType, ServiceContext context)
				throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public Element select(Dbms dbms, String table, String id, ServiceContext context)
				throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public Element selectNoXLinkResolver(Dbms dbms, String table, String id, boolean isIndexingTask)
				throws Exception {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Element internalSelect(Dbms dbms, String table, String id, boolean isIndexingTask)
				throws Exception {
			return super.internalSelect(dbms, table, id, isIndexingTask);
		}

	}
	private static final String OWNER_ID = "1234";
	final String metadata;
	{
		InputStream in = XmlSerializerTest.class.getResourceAsStream("valid-metadata.iso19139.xml");
		try {
			metadata = IOUtils.toString(in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@Test
	public void testInternalSelectHidingWithheldSettingsDisabled() throws Exception {
		assertHiddenElements(false, false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInternalSelectHidingWithheldNullServiceContext() throws Exception {

		Field field = ServiceContext.class.getDeclaredField("threadLocalInstance");
		field.setAccessible(true);
		InheritableThreadLocal<ServiceContext> threadLocalInstance = (InheritableThreadLocal<ServiceContext>) field.get(null);
		threadLocalInstance.set(null);
		assertHiddenElements(true);
	}

	@Test
	public void testInternalSelectHidingWithheldAdministrator() throws Exception {
		mockServiceContext(true);
		
		assertHiddenElements(false);
	}

	@Test
	public void testInternalSelectHidingWithheldNotLoggedIn() throws Exception {
		mockServiceContext(false);
		
		assertHiddenElements(true);
	}

	@Test
	public void testInternalCompleteHidingHiddenElement() throws Exception {
		mockServiceContext(false);
		
		SettingManager settingManager = mockSettingManager(true, false);
		XmlSerializer xmlSerializer = new DummyXmlSerializer(settingManager);
		
		Dbms dbms = mockDbms();
		
		Element loadedMetadata = xmlSerializer.internalSelect(dbms, "metadata", "1", false);
		List<?> withheld = Xml.selectNodes(loadedMetadata, "*//*[@gco:nilReason = 'withheld']", Arrays.asList(Geonet.Namespaces.GCO));

		assertEquals(0, withheld.size());
	}

	@Test
	public void testInternalSelectHidingWithheldNotOwner() throws Exception {
		mockServiceContext(false);
		
		assertHiddenElements(true);
	}
	
	@Test
	public void testInternalSelectHidingWithheldOwner() throws Exception {
		mockServiceContext(true);
		
		assertHiddenElements(false);
	}

	/**
	 * @param userId null if not logged in non-null to login
	 */
	private ServiceContext mockServiceContext(boolean canEdit) throws Exception{//boolean isAdmin, String userId) {
        ServiceContext context = mock(ServiceContext.class);
        doCallRealMethod().when(context).setAsThreadLocal();

        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.canEdit(any(ServiceContext.class), anyString())).thenReturn(canEdit);
        GeonetContext gc = mock(GeonetContext.class);
        when(gc.getAccessManager()).thenReturn(accessManager);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(gc);
        
		context.setAsThreadLocal();
		return context;
	}

	private void assertHiddenElements(boolean checkElementsAreHidden) throws Exception {
		assertHiddenElements(true, checkElementsAreHidden);
	}

	private void assertHiddenElements(boolean isEnabled, boolean checkElementsAreHidden) throws Exception {
		final int numberMdResolution;
		final int numberAttributes;
		if(checkElementsAreHidden) {
			numberMdResolution = 0;
			numberAttributes = 1;
		} else {
			numberMdResolution = 1;
			numberAttributes = 2;
		}

		SettingManager settingManager = mockSettingManager(isEnabled);
		XmlSerializer xmlSerializer = new DummyXmlSerializer(settingManager);
		
		Dbms dbms = mockDbms();
		
		Element loadedMetadata = xmlSerializer.internalSelect(dbms, "metadata", "1", false);
		List<?> resolutionElem = Xml.selectNodes(loadedMetadata, "*//gmd:MD_Resolution", Arrays.asList(Geonet.Namespaces.GMD));
		assertEquals(numberMdResolution, resolutionElem.size());
		
		@SuppressWarnings("unchecked")
		List<Element> withheld = (List<Element>) Xml.selectNodes(loadedMetadata, "*//*[@gco:nilReason = 'withheld']", Arrays.asList(Geonet.Namespaces.GCO));
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
		
		if(checkElementsAreHidden) {
			assertEquals("", withheld.get(0).getText());
		}
	}

	private Dbms mockDbms() throws SQLException {
		Element record = new Element(Jeeves.Elem.RECORD).addContent(new Element("data").addContent(metadata));
		record.addContent(new Element("owner").setText(OWNER_ID));
		Element response = new Element("result").addContent(record);
		Dbms dbms = mock(Dbms.class);
		when(dbms.select(anyString())).thenReturn(response);
		when(dbms.select(anyString(), anyVararg())).thenReturn(response);
		when(dbms.select(anyString(), anyMapOf(String.class, String.class))).thenReturn(response);
		return dbms;
	}

	private SettingManager mockSettingManager(boolean enabled) {
		return mockSettingManager(enabled, true);
	}
	private SettingManager mockSettingManager(boolean enabled, boolean keepmarkedelem) {
		SettingManager settingManager = mock(SettingManager.class);
		when(settingManager.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/enable", false)).thenReturn(enabled);
		when(settingManager.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/enable")).thenReturn(enabled);
		when(settingManager.getValue("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/enable")).thenReturn(Boolean.toString(enabled));

		when(settingManager.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/keepMarkedElement", false)).thenReturn(keepmarkedelem);
		when(settingManager.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/keepMarkedElement")).thenReturn(keepmarkedelem);
		when(settingManager.getValue("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/keepMarkedElement")).thenReturn(Boolean.toString(keepmarkedelem));
		return settingManager;
	}

}
