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
package org.fao.geonet.api.processing;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class XslProcessApiTest {

    private ServiceContext serviceContext;
    private DataManager dataManager;
    private IMetadataUtils metadataUtils;
    private ConfigurableApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;

    @Before
    public void setUp() {
        serviceContext = mock(ServiceContext.class);
        dataManager = mock(DataManager.class);
        metadataUtils = mock(IMetadataUtils.class);
        applicationContext = mock(ConfigurableApplicationContext.class);
        transactionManager = mock(PlatformTransactionManager.class);

        when(serviceContext.getBean(DataManager.class)).thenReturn(dataManager);
        when(serviceContext.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
        when(applicationContext.getBean(PlatformTransactionManager.class)).thenReturn(transactionManager);

        // Mock transaction status
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        ApplicationContextHolder.set(applicationContext);
    }

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    @Test
    public void testBatchProcessing() throws Exception {
        Set<String> records = Sets.newHashSet();
        for (int i = 0; i < 150; i++) {
            records.add("uuid-" + i);
        }

        String process = "test-process";
        HttpSession session = mock(HttpSession.class);
        String siteURL = "http://localhost:8080/geonetwork";
        XsltMetadataProcessingReport report = new XsltMetadataProcessingReport(process);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Mock metadataUtils to return an empty list for each UUID
        when(metadataUtils.findAllIdsBy(any())).thenReturn(Collections.emptyList());

        XslProcessApi.BatchXslMetadataReindexer reindexer = new XslProcessApi.BatchXslMetadataReindexer(
            serviceContext, dataManager, records, process, session, siteURL, report, request, true, true, 1
        );

        reindexer.process("catalogue");

        // Verify that TransactionManager.runInTransaction was effectively called twice
        // We check this by verifying that transactionManager.getTransaction was called twice
        // since runInTransaction calls it once per batch.
        verify(transactionManager, times(2)).getTransaction(any());

        // Verify that findAllIdsBy was called for each record (150 times)
        verify(metadataUtils, times(150)).findAllIdsBy(any());
    }
}
