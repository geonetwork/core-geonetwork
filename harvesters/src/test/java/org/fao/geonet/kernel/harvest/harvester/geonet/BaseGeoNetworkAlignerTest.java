//=============================================================================
//===    Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.GeonetParams;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests for the NullPointerException fixed in {@link BaseGeoNetworkAligner#updateMetadata}
 * when a {@link RecordInfo} coming from the local catalog has a {@code null} schema (see #9351).
 */
public class BaseGeoNetworkAlignerTest {

    private static final String DETECTED_SCHEMA = "iso19139";

    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private IMetadataIndexer metadataIndexer;
    private MetadataRepository metadataRepository;
    private GeonetParams params;
    private BaseGeoNetworkAligner<GeonetParams> aligner;

    @Before
    public void setUp() throws Exception {
        GeonetContext geonetContext = mock(GeonetContext.class);
        context = mock(ServiceContext.class);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(geonetContext);

        dataMan = mock(DataManager.class);
        metadataManager = mock(IMetadataManager.class);
        metadataIndexer = mock(IMetadataIndexer.class);
        metadataRepository = mock(MetadataRepository.class);
        IMetadataUtils metadataUtils = mock(IMetadataUtils.class);
        when(metadataUtils.findAllSimple(anyString())).thenReturn(new ArrayList<>());

        when(geonetContext.getBean(IMetadataIndexer.class)).thenReturn(metadataIndexer);
        when(geonetContext.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        when(geonetContext.getBean(IMetadataOperations.class)).thenReturn(mock(IMetadataOperations.class));
        when(geonetContext.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
        when(geonetContext.getBean(IMetadataSchemaUtils.class)).thenReturn(mock(IMetadataSchemaUtils.class));
        when(geonetContext.getBean(MetadataRepository.class)).thenReturn(metadataRepository);
        when(geonetContext.getBean(SettingManager.class)).thenReturn(mock(SettingManager.class));
        when(geonetContext.getBean(AccessManager.class)).thenReturn(mock(AccessManager.class));
        when(geonetContext.getBean(DataManager.class)).thenReturn(dataMan);

        // Beans looked up directly on the ServiceContext by addPrivileges()/addCategories(),
        // invoked further down in updateMetadata() after the schema comparison being tested.
        when(context.getBean(DataManager.class)).thenReturn(dataMan);
        when(context.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        when(context.getBean(OperationAllowedRepository.class)).thenReturn(mock(OperationAllowedRepository.class));
        MetadataCategoryRepository metadataCategoryRepository = mock(MetadataCategoryRepository.class);
        when(metadataCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(context.getBean(MetadataCategoryRepository.class)).thenReturn(metadataCategoryRepository);
        when(context.getLanguage()).thenReturn("eng");

        params = new GeonetParams(mock(DataManager.class));
        params.xslfilter = "";
        params.setValidate(HarvestValidationEnum.NOVALIDATION);

        aligner = new BaseGeoNetworkAligner<GeonetParams>(new AtomicBoolean(false), mock(Logger.class), context, params) {
            @Override
            protected Path retrieveMEF(String uuid) throws URISyntaxException {
                throw new UnsupportedOperationException("not used by this test");
            }
        };
        // Populated by align() in production code; updateMetadata() is exercised directly here.
        aligner.localUuids = new UUIDMapper(metadataUtils, params.getUuid());
    }

    private Element newInfoElement() {
        Element info = new Element("info");
        info.addContent(new Element(BaseGeoNetworkAligner.GENERAL));
        return info;
    }

    /**
     * Reproduces #9351: a RecordInfo built with the 2-arg constructor (as used when the local
     * catalog entry doesn't carry the schema) has a null {@code schema} field. Comparing it with
     * {@code ri.schema.equals(schema)} throws an NPE; the fix compares the other way around.
     */
    @Test
    public void updateMetadataDoesNotThrowWhenCatalogSchemaIsNull() throws Throwable {
        Metadata metadata = new Metadata();
        when(dataMan.autodetectSchema(any(Element.class), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataRepository.findOneById(1)).thenReturn(metadata);

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00");

        invokeUpdateMetadata(ri, "1", new Element("root"), newInfoElement(), true, true);

        assertEquals("Schema mismatch (null vs detected) must be detected and applied",
            DETECTED_SCHEMA, metadata.getDataInfo().getSchemaId());
    }

    @Test
    public void updateMetadataDoesNotFlagSchemaChangeWhenSchemasMatch() throws Throwable {
        Metadata metadata = new Metadata();
        when(dataMan.autodetectSchema(any(Element.class), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataRepository.findOneById(1)).thenReturn(metadata);

        RecordInfo ri = new RecordInfo("uuid-1", "2020-01-01T00:00:00", DETECTED_SCHEMA, null);

        invokeUpdateMetadata(ri, "1", new Element("root"), newInfoElement(), true, true);

        assertNull("Schema already matches, setSchemaId() should not have been called",
            metadata.getDataInfo().getSchemaId());
    }

    /**
     * Regression tests for workstream 9 (MEF 3.0 support): {@link BaseGeoNetworkAligner#existsFile}
     * and {@link BaseGeoNetworkAligner#removeOldFile} used to receive a raw {@code <public>}/
     * {@code <private>} {@link Element} straight from info.xml; since a MEF 3.0 archive has
     * neither, harvesting one used to silently skip every attachment. They now receive the
     * {@code List<Element>} produced by {@link MEFLib#getFilesElement}, which understands both
     * the MEF 3.0 {@code <store>} format and the legacy format.
     */
    @Test
    public void existsFileMatchesFileRegisteredUnderMef30StoreElement() {
        Element info = new Element("info");
        Element store = new Element("store");
        store.addContent(new Element("file").setAttribute("name", "a.txt").setAttribute("access", "public"));
        info.addContent(store);

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        assertTrue(aligner.existsFile("a.txt", publicFiles));
        assertFalse(aligner.existsFile("missing.txt", publicFiles));
    }

    @Test
    public void existsFileStillMatchesFileRegisteredUnderLegacyPublicElement() {
        Element info = new Element("info");
        Element legacyPublic = new Element("public");
        legacyPublic.addContent(new Element("file").setAttribute("name", "b.txt"));
        info.addContent(legacyPublic);

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        assertTrue(aligner.existsFile("b.txt", publicFiles));
        assertFalse(aligner.existsFile("missing.txt", publicFiles));
    }

    /**
     * Reproduces the pre-existing bug where {@code removeOldFile} compared a resource's composite
     * {@code getId()} (e.g. {@code "uuid-1/attachments/keep.txt"}) against the bare filename in
     * info.xml's {@code name} attribute, so the comparison could never match and every existing
     * resource was deleted on every harvest sync - even ones still listed in the remote info.xml.
     */
    @Test
    public void removeOldFileKeepsResourceStillListedInInfoFiles() throws Exception {
        Store store = mock(Store.class);
        MetadataResource keep = mock(MetadataResource.class);
        when(keep.getFilename()).thenReturn("keep.txt");
        when(keep.getId()).thenReturn("uuid-1/attachments/keep.txt");
        when(store.getResources(context, "uuid-1", MetadataResourceVisibility.PUBLIC, null, true))
            .thenReturn(Collections.singletonList(keep));

        Element info = new Element("info");
        Element storeEl = new Element("store");
        storeEl.addContent(new Element("file").setAttribute("name", "keep.txt").setAttribute("access", "public"));
        info.addContent(storeEl);
        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        aligner.removeOldFile(context, mock(Logger.class), store, "uuid-1", publicFiles, MetadataResourceVisibility.PUBLIC);

        verify(store, never()).delResource(any(), anyString(), any(MetadataResourceVisibility.class), anyString(), any());
    }

    @Test
    public void removeOldFileDeletesResourceNoLongerListedInInfoFiles() throws Exception {
        Store store = mock(Store.class);
        MetadataResource stale = mock(MetadataResource.class);
        when(stale.getFilename()).thenReturn("stale.txt");
        when(stale.getId()).thenReturn("uuid-1/attachments/stale.txt");
        when(store.getResources(context, "uuid-1", MetadataResourceVisibility.PUBLIC, null, true))
            .thenReturn(Collections.singletonList(stale));

        Element info = new Element("info");
        Element storeEl = new Element("store");
        storeEl.addContent(new Element("file").setAttribute("name", "keep.txt").setAttribute("access", "public"));
        info.addContent(storeEl);
        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        aligner.removeOldFile(context, mock(Logger.class), store, "uuid-1", publicFiles, MetadataResourceVisibility.PUBLIC);

        verify(store, times(1)).delResource(context, "uuid-1", MetadataResourceVisibility.PUBLIC, "stale.txt", true);
    }

    private void invokeUpdateMetadata(RecordInfo ri, String id, Element md, Element info,
                                      boolean localRating, boolean force) throws Throwable {
        Method method = BaseGeoNetworkAligner.class.getDeclaredMethod("updateMetadata",
            RecordInfo.class, String.class, Element.class, Element.class, boolean.class, boolean.class);
        method.setAccessible(true);
        try {
            method.invoke(aligner, ri, id, md, info, localRating, force);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
