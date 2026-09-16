//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.harvest.harvester.csw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests for the NullPointerException fixed in {@link Aligner#updatingLocalMetadata}
 * when a {@link RecordInfo} coming from the local catalog has a {@code null} schema (see #9351).
 */
public class AlignerTest {

    private static final String DETECTED_SCHEMA = "iso19139";

    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private GetRecordByIdRequest request;
    private CswParams params;
    private Aligner aligner;

    @Before
    public void setUp() throws Exception {
        GeonetContext geonetContext = mock(GeonetContext.class);
        context = mock(ServiceContext.class);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(geonetContext);

        dataMan = mock(DataManager.class);
        metadataManager = mock(IMetadataManager.class);

        // Beans looked up on the GeonetContext by the Aligner constructor.
        when(geonetContext.getBean(DataManager.class)).thenReturn(dataMan);
        when(geonetContext.getBean(IMetadataUtils.class)).thenReturn(mock(IMetadataUtils.class));
        when(geonetContext.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        when(geonetContext.getBean(IMetadataIndexer.class)).thenReturn(mock(IMetadataIndexer.class));
        when(geonetContext.getBean(EsSearchManager.class)).thenReturn(mock(EsSearchManager.class));
        when(geonetContext.getBean(SettingManager.class)).thenReturn(mock(SettingManager.class));

        // The GetRecordByIdRequest built by the constructor needs an HTTP request factory.
        GeonetHttpRequestFactory httpRequestFactory = mock(GeonetHttpRequestFactory.class);
        when(httpRequestFactory.createXmlRequest(any(), anyInt(), any())).thenReturn(mock(XmlRequest.class));
        when(context.getBean(GeonetHttpRequestFactory.class)).thenReturn(httpRequestFactory);

        // Beans looked up directly on the ServiceContext by addPrivileges()/addCategories(),
        // invoked further down in updatingLocalMetadata() after the schema comparison being tested.
        when(context.getBean(DataManager.class)).thenReturn(dataMan);
        when(context.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        when(context.getBean(OperationAllowedRepository.class)).thenReturn(mock(OperationAllowedRepository.class));
        MetadataCategoryRepository metadataCategoryRepository = mock(MetadataCategoryRepository.class);
        when(metadataCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(context.getBean(MetadataCategoryRepository.class)).thenReturn(metadataCategoryRepository);
        when(context.getLanguage()).thenReturn("eng");

        // A CSW server exposing a GetRecordById GET endpoint.
        CswServer server = mock(CswServer.class);
        CswOperation operation = mock(CswOperation.class);
        when(operation.getGetUrl()).thenReturn(new URL("http://localhost/csw"));
        when(server.getOperation(CswServer.GET_RECORD_BY_ID)).thenReturn(operation);

        params = new CswParams(mock(DataManager.class));
        params.xslfilter = "";
        params.setValidate(HarvestValidationEnum.NOVALIDATION);

        aligner = new Aligner(new AtomicBoolean(false), context, server, params, mock(Logger.class));

        // Replace the real request built by the constructor with a mock returning a
        // canned GetRecordById response, so updatingLocalMetadata() can be exercised offline.
        request = mock(GetRecordByIdRequest.class);
        setField(aligner, "request", request);
    }

    /**
     * Stubs the GetRecordById retrieval and the metadata update so that
     * {@link Aligner#updatingLocalMetadata} reaches the schema-comparison branch and returns
     * the metadata whose schema id is asserted by the tests.
     */
    private Metadata stubRetrievedRecord() throws Exception {
        Element response = new Element("GetRecordByIdResponse");
        response.addContent(new Element("MD_Metadata"));
        when(request.execute()).thenReturn(response);

        when(dataMan.autodetectSchema(any(Element.class), any())).thenReturn(DETECTED_SCHEMA);

        Metadata metadata = new Metadata();
        when(metadataManager.updateMetadata(eq(context), eq("1"), any(Element.class), eq(false), eq(false),
            anyString(), any(), eq(true), eq(IndexingMode.none))).thenReturn(metadata);
        return metadata;
    }

    /**
     * Reproduces #9351: a RecordInfo built with the 2-arg constructor (as used when the local
     * catalog entry doesn't carry the schema) has a null {@code schema} field. Comparing it with
     * {@code ri.schema.equals(schema)} throws an NPE; the fix compares the other way around.
     */
    @Test
    public void updatingLocalMetadataDoesNotThrowWhenCatalogSchemaIsNull() throws Exception {
        Metadata metadata = stubRetrievedRecord();

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00");

        boolean updated = aligner.updatingLocalMetadata(ri, "1", false);

        assertTrue(updated);
        assertEquals("Schema mismatch (null vs detected) must be detected and applied",
            DETECTED_SCHEMA, metadata.getDataInfo().getSchemaId());
    }

    @Test
    public void updatingLocalMetadataDoesNotFlagSchemaChangeWhenSchemasMatch() throws Exception {
        Metadata metadata = stubRetrievedRecord();

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00", DETECTED_SCHEMA, null);

        boolean updated = aligner.updatingLocalMetadata(ri, "1", false);

        assertTrue(updated);
        assertNull("Schema already matches, setSchemaId() should not have been called",
            metadata.getDataInfo().getSchemaId());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = Aligner.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
