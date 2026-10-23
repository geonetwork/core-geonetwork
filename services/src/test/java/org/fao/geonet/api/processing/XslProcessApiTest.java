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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @Test
    public void testBatchProcessingContinuesAfterRecordFailure() throws Exception {
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

        // Assign each uuid a distinct metadata id, in call order
        AtomicInteger idSequence = new AtomicInteger(0);
        when(metadataUtils.findAllIdsBy(any())).thenAnswer(invocation -> Collections.singletonList(idSequence.getAndIncrement()));

        // Every record fails as soon as its metadata is fetched, before any real XSL
        // processing is attempted, so this stays a fast, dependency-free unit test.
        when(dataManager.getMetadata(any(), anyString(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenThrow(new RuntimeException("Simulated failure fetching metadata"));
        when(metadataUtils.findOne(anyInt())).thenReturn(null);

        XslProcessApi.BatchXslMetadataReindexer reindexer = new XslProcessApi.BatchXslMetadataReindexer(
            serviceContext, dataManager, records, process, session, siteURL, report, request, true, true, 1
        );

        // Must not throw: a failure on one record must not abort the whole run.
        reindexer.process("catalogue");

        // Every uuid must still be looked up, including those in batches after the first failure.
        verify(metadataUtils, times(150)).findAllIdsBy(any());

        // Both batches (150 records / batch size 100) must still run their own transaction.
        verify(transactionManager, times(2)).getTransaction(any());

        // Every failure is captured in the report rather than silently dropped.
        assertEquals(150, report.getNumberOfRecordsWithErrors());
    }
}
