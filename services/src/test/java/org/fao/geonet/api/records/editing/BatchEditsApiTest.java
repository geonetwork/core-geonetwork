/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.api.records.editing;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.constants.Jeeves;
import org.fao.geonet.kernel.BatchEditParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.domain.Profile;
import java.util.*;

public class BatchEditsApiTest {

    private BatchEditsApi api;
    private ServiceContext serviceContext;
    private DataManager dataManager;
    private IMetadataUtils metadataUtils;
    private ConfigurableApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;
    private SchemaManager schemaManager;
    private AccessManager accessManager;
    private SettingManager settingManager;
    private RoleHierarchy roleHierarchy;

    private UserSession userSession;
    private HttpSession session;

    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        api = new BatchEditsApi();
        serviceContext = mock(ServiceContext.class);
        dataManager = mock(DataManager.class);
        metadataUtils = mock(IMetadataUtils.class);
        applicationContext = mock(ConfigurableApplicationContext.class);
        transactionManager = mock(PlatformTransactionManager.class);
        schemaManager = mock(SchemaManager.class);
        accessManager = mock(AccessManager.class);
        settingManager = mock(SettingManager.class);
        roleHierarchy = mock(RoleHierarchy.class);
        userSession = mock(UserSession.class);
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);

        api.setApplicationContext(applicationContext);
        // We need to inject mocks into BatchEditsApi fields
        java.lang.reflect.Field schemaManagerField = BatchEditsApi.class.getDeclaredField("_schemaManager");
        schemaManagerField.setAccessible(true);
        schemaManagerField.set(api, schemaManager);

        java.lang.reflect.Field settingManagerField = BatchEditsApi.class.getDeclaredField("settingManager");
        settingManagerField.setAccessible(true);
        settingManagerField.set(api, settingManager);

        java.lang.reflect.Field roleHierarchyField = BatchEditsApi.class.getDeclaredField("roleHierarchy");
        roleHierarchyField.setAccessible(true);
        roleHierarchyField.set(api, roleHierarchy);

        when(applicationContext.getBean(DataManager.class)).thenReturn(dataManager);
        when(applicationContext.getBean(SchemaManager.class)).thenReturn(schemaManager);
        when(applicationContext.getBean(AccessManager.class)).thenReturn(accessManager);
        when(applicationContext.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
        when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(transactionManager);

        // Mock transaction status
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        ApplicationContextHolder.set(applicationContext);

        LanguageUtils languageUtils = mock(LanguageUtils.class);
        ServiceManager serviceManager = mock(ServiceManager.class);
        when(applicationContext.getBean(LanguageUtils.class)).thenReturn(languageUtils);
        when(applicationContext.getBean(ServiceManager.class)).thenReturn(serviceManager);
        when(languageUtils.getIso3langCode(any())).thenReturn("eng");
        when(serviceManager.createServiceContext(anyString(), anyString(), any(HttpServletRequest.class))).thenReturn(serviceContext);

        when(serviceContext.getUserSession()).thenReturn(userSession);
        when(userSession.getProfile()).thenReturn(Profile.Administrator);

        when(session.getAttribute(Jeeves.Elem.SESSION)).thenReturn(userSession);
        when(request.getSession()).thenReturn(session);

        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));
    }

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    @Test
    public void testBatchProcessing() throws Exception {
        MetadataSchema metadataSchema = mock(MetadataSchema.class);
        when(schemaManager.getSchema(anyString())).thenReturn(metadataSchema);
        when(metadataSchema.getNamespaces()).thenReturn(new java.util.ArrayList<>());

        int numRecords = 150;
        String[] uuids = new String[numRecords];
        for (int i = 0; i < numRecords; i++) {
            uuids[i] = "uuid-" + i;
            AbstractMetadata record = mock(AbstractMetadata.class);
            when(record.getId()).thenReturn(i);
            MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
            when(record.getDataInfo()).thenReturn(dataInfo);
            when(dataInfo.getSchemaId()).thenReturn("iso19139");
            when(record.getXmlData(false)).thenReturn(new org.jdom.Element("root"));

            when(metadataUtils.findOneByUuid(uuids[i])).thenReturn(record);
            when(accessManager.isOwner(any(), anyString())).thenReturn(true);
        }

        BatchEditParameter[] edits = new BatchEditParameter[1];
        edits[0] = new BatchEditParameter();
        edits[0].setXpath("/root/element");
        edits[0].setValue("value");

        api.batchEdit(uuids, null, false, edits, request);

        // Verify that TransactionManager.runInTransaction was effectively called twice
        verify(transactionManager, times(2)).getTransaction(any());
    }

    @Test
    public void testBatchProcessingContinuesAfterRecordFailure() throws Exception {
        when(schemaManager.getSchema(anyString())).thenThrow(new RuntimeException("Simulated schema failure"));

        int numRecords = 150;
        String[] uuids = new String[numRecords];
        for (int i = 0; i < numRecords; i++) {
            uuids[i] = "uuid-" + i;
            AbstractMetadata record = mock(AbstractMetadata.class);
            when(record.getId()).thenReturn(i);
            MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
            when(record.getDataInfo()).thenReturn(dataInfo);
            when(dataInfo.getSchemaId()).thenReturn("iso19139");
            when(record.getXmlData(false)).thenReturn(new org.jdom.Element("root"));

            when(metadataUtils.findOneByUuid(uuids[i])).thenReturn(record);
            when(accessManager.isOwner(any(), anyString())).thenReturn(true);
        }

        BatchEditParameter[] edits = new BatchEditParameter[1];
        edits[0] = new BatchEditParameter();
        edits[0].setXpath("/root/element");
        edits[0].setValue("value");

        // Must not throw: a failure processing one record must not abort the whole batch.
        IProcessingReport report = api.batchEdit(uuids, null, false, edits, request);

        // Both batches (150 records / batch size 100) must still run their own transaction.
        verify(transactionManager, times(2)).getTransaction(any());

        // Every record failed, but every failure is captured rather than aborting the run.
        SimpleMetadataProcessingReport simpleReport = (SimpleMetadataProcessingReport) report;
        assertEquals(numRecords, simpleReport.getNumberOfRecordsWithErrors());
        assertEquals(numRecords, simpleReport.getNumberOfRecordsProcessed());
    }
}
