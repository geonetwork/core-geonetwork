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
package org.fao.geonet.kernel.harvest.harvester.sftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
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
    private IMetadataSchemaUtils metadataSchemaUtils;
    private IMetadataManager metadataManager;
    private SftpParams params;
    private Aligner aligner;

    @Before
    public void setUp() {
        context = mock(ServiceContext.class);
        metadataManager = mock(IMetadataManager.class);
        metadataSchemaUtils = mock(IMetadataSchemaUtils.class);

        when(context.getBean(IMetadataUtils.class)).thenReturn(mock(IMetadataUtils.class));
        when(context.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        when(context.getBean(IMetadataIndexer.class)).thenReturn(mock(IMetadataIndexer.class));
        when(context.getBean(IMetadataSchemaUtils.class)).thenReturn(metadataSchemaUtils);

        // Beans looked up directly by addPrivileges()/addCategories(), invoked further down in
        // updatingLocalMetadata() after the schema comparison being tested.
        when(context.getBean(DataManager.class)).thenReturn(mock(DataManager.class));
        when(context.getBean(OperationAllowedRepository.class)).thenReturn(mock(OperationAllowedRepository.class));
        MetadataCategoryRepository metadataCategoryRepository = mock(MetadataCategoryRepository.class);
        when(metadataCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(context.getBean(MetadataCategoryRepository.class)).thenReturn(metadataCategoryRepository);
        when(context.getLanguage()).thenReturn("eng");

        params = new SftpParams(mock(DataManager.class));
        params.xslfilter = "";

        aligner = new Aligner(new AtomicBoolean(false), context, params, mock(Logger.class));
    }

    /**
     * Reproduces #9351: a RecordInfo built with the 2-arg constructor (as used when the local
     * catalog entry doesn't carry the schema) has a null {@code schema} field. Comparing it with
     * {@code ri.schema.equals(schema)} throws an NPE; the fix compares the other way around.
     */
    @Test
    public void updatingLocalMetadataDoesNotThrowWhenCatalogSchemaIsNull() throws Exception {
        Element md = new Element("root");
        Metadata metadata = new Metadata();

        when(metadataSchemaUtils.autodetectSchema(eq(md), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataManager.updateMetadata(eq(context), eq("1"), eq(md), eq(false), eq(false),
            anyString(), any(), eq(true), eq(IndexingMode.none))).thenReturn(metadata);

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00");

        boolean updated = aligner.updatingLocalMetadata(ri, "1", md, false);

        assertTrue(updated);
        assertEquals("Schema mismatch (null vs detected) must be detected and applied",
            DETECTED_SCHEMA, metadata.getDataInfo().getSchemaId());
    }

    @Test
    public void updatingLocalMetadataDoesNotFlagSchemaChangeWhenSchemasMatch() throws Exception {
        Element md = new Element("root");
        Metadata metadata = new Metadata();

        when(metadataSchemaUtils.autodetectSchema(eq(md), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataManager.updateMetadata(eq(context), eq("1"), eq(md), eq(false), eq(false),
            anyString(), any(), eq(true), eq(IndexingMode.none))).thenReturn(metadata);

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00", DETECTED_SCHEMA, null);

        boolean updated = aligner.updatingLocalMetadata(ri, "1", md, false);

        assertTrue(updated);
        assertNull("Schema already matches, setSchemaId() should not have been called",
            metadata.getDataInfo().getSchemaId());
    }
}
