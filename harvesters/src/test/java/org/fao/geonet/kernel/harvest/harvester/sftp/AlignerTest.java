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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests for the SFTP {@link Aligner}, covering:
 * <ul>
 *   <li>The NullPointerException fixed in {@link Aligner#updatingLocalMetadata} when a
 *       {@link RecordInfo} coming from the local catalog has a {@code null} schema (see #9351).</li>
 *   <li>The UUID collision {@code SKIP} policy being resolved <em>before</em> validation, so a
 *       record that already exists from another source is skipped without being validated
 *       (see #9432).</li>
 * </ul>
 */
public class AlignerTest {

    private static final String DETECTED_SCHEMA = "iso19139";

    private ServiceContext context;
    private IMetadataSchemaUtils metadataSchemaUtils;
    private IMetadataManager metadataManager;
    private IMetadataUtils metadataUtils;
    private SftpParams params;
    private Aligner aligner;

    @Before
    public void setUp() {
        context = mock(ServiceContext.class);
        metadataManager = mock(IMetadataManager.class);
        metadataSchemaUtils = mock(IMetadataSchemaUtils.class);
        metadataUtils = mock(IMetadataUtils.class);

        when(context.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
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

    // ----------------------------------------------------------------------------------------
    // #9432 - UUID collision SKIP must be resolved before validation.
    //
    // The decision has two parts, now shared across the database, local filesystem and sftp
    // harvesters:
    //   1. detection - isCollisionFromOtherSource(existingId, uuid): the record exists in the
    //      catalogue but does not belong to this harvester (a genuine collision). The existing id
    //      is resolved once by getExistingMetadataId() and reused by insertOrUpdate();
    //   2. policy    - AbstractParams.isSkippedByUuidCollision(collision): that collision must be
    //      skipped because the configured policy is SKIP.
    // When both hold, harvest() bypasses validation, so an invalid record that collides with an
    // existing one is counted as uuidSkipped instead of doesNotValidate.
    // ----------------------------------------------------------------------------------------

    @Test
    public void skipsCollisionWhenPolicyIsSkip() {
        params.setOverrideUuid(AbstractParams.OverrideUuid.SKIP);

        assertTrue("A collision from another source with SKIP policy must be skipped before validation",
            params.isSkippedByUuidCollision(true));
        assertFalse("Without a collision there is nothing to skip",
            params.isSkippedByUuidCollision(false));
    }

    @Test
    public void doesNotSkipCollisionWhenPolicyIsNotSkip() {
        params.setOverrideUuid(AbstractParams.OverrideUuid.OVERRIDE);
        assertFalse("OVERRIDE records must still be validated",
            params.isSkippedByUuidCollision(true));

        params.setOverrideUuid(AbstractParams.OverrideUuid.RANDOM);
        assertFalse("RANDOM records must still be validated",
            params.isSkippedByUuidCollision(true));
    }

    @Test
    public void detectsCollisionFromAnotherSource() throws Exception {
        // Record exists in the catalogue but does not belong to this harvester.
        when(metadataUtils.getMetadataId("uuid-1")).thenReturn("5");
        setLocalUuids(uuidMapperReturning("uuid-1", null));

        String existingId = invokeGetExistingMetadataId("uuid-1");
        assertTrue("An existing record owned by another source is a collision",
            invokeIsCollisionFromOtherSource(existingId, "uuid-1"));
    }

    @Test
    public void noCollisionWhenRecordBelongsToThisHarvester() throws Exception {
        // Record already belongs to this harvester -> it is an update, not a collision.
        setLocalUuids(uuidMapperReturning("uuid-1", "5"));

        assertFalse("A record owned by this harvester is not a collision",
            invokeIsCollisionFromOtherSource("5", "uuid-1"));
    }

    @Test
    public void noCollisionWhenRecordDoesNotExistYet() throws Exception {
        // No existing record -> it is a new insert, so there is no collision.
        setLocalUuids(uuidMapperReturning("uuid-1", null));

        assertFalse("A brand new record is not a collision",
            invokeIsCollisionFromOtherSource(null, "uuid-1"));
    }

    @Test
    public void resolvesExistingMetadataId() throws Exception {
        when(metadataUtils.getMetadataId("uuid-1")).thenReturn("5");

        assertEquals("The existing record id must be resolved for reuse",
            "5", invokeGetExistingMetadataId("uuid-1"));
    }

    @Test
    public void lookupFailureFallsBackToNormalPath() throws Exception {
        // If the id cannot be resolved, fall back to the normal validate/insert path: returning
        // null means the record is treated as not-a-collision (so it is validated) rather than
        // aborting the harvest.
        when(metadataUtils.getMetadataId("uuid-1")).thenThrow(new RuntimeException("boom"));
        setLocalUuids(uuidMapperReturning("uuid-1", null));

        String existingId = invokeGetExistingMetadataId("uuid-1");
        assertNull("A lookup failure must resolve to a null id", existingId);
        assertFalse("A lookup failure must fall back to the normal path, not silently skip",
            invokeIsCollisionFromOtherSource(existingId, "uuid-1"));
    }

    // End-to-end coverage of the actual #9432 fix through alignRecord(): the validation gating
    // wired into the harvest loop, not just the collision/skip predicates in isolation.

    @Test
    public void invalidCollidingRecordWithSkipPolicyIsSkippedNotValidated() throws Exception {
        // A record that already exists from another source, under the SKIP policy, and that would
        // fail validation if it were ever validated.
        params.setOverrideUuid(AbstractParams.OverrideUuid.SKIP);
        HarvestResult result = givenAValidationThatFailsWhenReached();

        Element md = givenARecord("uuid-1");
        givenCollisionFromAnotherSource("uuid-1", "5");

        aligner.alignRecord(md, new ArrayList<>());

        assertEquals("A colliding SKIP record must not be validated (see #9432)",
            0, result.doesNotValidate);
        assertEquals("A colliding SKIP record must be counted as skipped",
            1, result.uuidSkipped);
    }

    @Test
    public void invalidCollidingRecordWithNonSkipPolicyIsStillValidated() throws Exception {
        // The same collision, but with a non-SKIP policy: the record must still be validated and,
        // being invalid, rejected as doesNotValidate rather than inserted/updated.
        params.setOverrideUuid(AbstractParams.OverrideUuid.OVERRIDE);
        HarvestResult result = givenAValidationThatFailsWhenReached();

        Element md = givenARecord("uuid-1");
        givenCollisionFromAnotherSource("uuid-1", "5");

        aligner.alignRecord(md, new ArrayList<>());

        assertEquals("A non-SKIP collision must still be validated and, being invalid, rejected",
            1, result.doesNotValidate);
        assertEquals("A record rejected by validation must not be skipped/inserted",
            0, result.uuidSkipped);
    }

    private Element givenARecord(String uuid) throws Exception {
        Element md = new Element("root");
        when(metadataSchemaUtils.autodetectSchema(eq(md), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataUtils.extractUUID(DETECTED_SCHEMA, md)).thenReturn(uuid);
        when(metadataUtils.extractDateModified(DETECTED_SCHEMA, md)).thenReturn("2020-01-01T00:00:00");
        return md;
    }

    private void givenCollisionFromAnotherSource(String uuid, String existingId) throws Exception {
        // Record exists in the catalogue (existingId) but does not belong to this harvester.
        when(metadataUtils.getMetadataId(uuid)).thenReturn(existingId);
        setLocalUuids(uuidMapperReturning(uuid, null));
    }

    /**
     * Configures XSD validation backed by a {@link SchemaManager} that always throws, so validation
     * fails whenever it is actually reached. The tests assert on the resulting counters to prove
     * whether validation ran, and returns the {@link HarvestResult} wired into the aligner.
     */
    private HarvestResult givenAValidationThatFailsWhenReached() throws Exception {
        params.setValidate(HarvestValidationEnum.XSDVALIDATION);
        SchemaManager schemaManager = mock(SchemaManager.class);
        when(schemaManager.autodetectSchema(any())).thenThrow(new IllegalStateException("invalid metadata"));
        when(context.getBean(SchemaManager.class)).thenReturn(schemaManager);
        return setHarvestResult();
    }

    private HarvestResult setHarvestResult() throws Exception {
        HarvestResult result = new HarvestResult();
        Field field = Aligner.class.getDeclaredField("result");
        field.setAccessible(true);
        field.set(aligner, result);
        return result;
    }

    private String invokeGetExistingMetadataId(String uuid) throws Exception {
        Method method = Aligner.class.getDeclaredMethod("getExistingMetadataId", String.class);
        method.setAccessible(true);
        return (String) method.invoke(aligner, uuid);
    }

    private boolean invokeIsCollisionFromOtherSource(String existingId, String uuid) throws Exception {
        Method method = Aligner.class.getDeclaredMethod("isCollisionFromOtherSource", String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(aligner, existingId, uuid);
    }

    private void setLocalUuids(UUIDMapper mapper) throws Exception {
        Field field = Aligner.class.getDeclaredField("localUuids");
        field.setAccessible(true);
        field.set(aligner, mapper);
    }

    private UUIDMapper uuidMapperReturning(String uuid, String id) {
        UUIDMapper mapper = mock(UUIDMapper.class);
        when(mapper.getID(uuid)).thenReturn(id);
        return mapper;
    }
}
