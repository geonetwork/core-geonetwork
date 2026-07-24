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
package org.fao.geonet.kernel.harvest.harvester.database;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests for #9432 on the database harvester: the UUID collision {@code SKIP} policy is
 * resolved <em>before</em> validation, so a record that already exists from another source is
 * counted as {@code uuidSkipped} without being validated (and therefore never ends up as
 * {@code doesNotValidate}).
 */
public class DatabaseHarvesterAlignerTest {

    private static final String DETECTED_SCHEMA = "iso19139";
    private static final String UUID = "uuid-1";
    private static final String EXISTING_ID = "5";

    private ServiceContext context;
    private IMetadataSchemaUtils metadataSchemaUtils;
    private IMetadataUtils metadataUtils;
    private DatabaseHarvesterParams params;
    private DatabaseHarvesterAligner aligner;

    @Before
    public void setUp() {
        context = mock(ServiceContext.class);
        metadataSchemaUtils = mock(IMetadataSchemaUtils.class);
        metadataUtils = mock(IMetadataUtils.class);

        GeonetContext gc = mock(GeonetContext.class);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(gc);
        when(gc.getBean(DataManager.class)).thenReturn(mock(DataManager.class));
        when(gc.getBean(IMetadataManager.class)).thenReturn(mock(IMetadataManager.class));
        when(gc.getBean(IMetadataSchemaUtils.class)).thenReturn(metadataSchemaUtils);
        when(gc.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
        when(gc.getBean(IMetadataIndexer.class)).thenReturn(mock(IMetadataIndexer.class));
        when(gc.getBean(MetadataRepository.class)).thenReturn(mock(MetadataRepository.class));

        params = new DatabaseHarvesterParams(mock(DataManager.class));
        params.setUuid("harvester-uuid");

        aligner = new DatabaseHarvesterAligner(new AtomicBoolean(false), mock(Logger.class),
            context, params, new ArrayList<>());
    }

    @Test
    public void invalidCollidingRecordWithSkipPolicyIsSkippedNotValidated() throws Exception {
        params.setOverrideUuid(AbstractParams.OverrideUuid.SKIP);
        givenAValidationThatFailsWhenReached();
        Element md = givenARecord();
        givenCollisionFromAnotherSource();

        aligner.processMetadata(md);

        HarvestResult result = harvestResult();
        assertEquals("A colliding SKIP record must not be validated (see #9432)",
            0, result.doesNotValidate);
        assertEquals("A colliding SKIP record must be counted as skipped",
            1, result.uuidSkipped);
    }

    @Test
    public void invalidCollidingRecordWithNonSkipPolicyIsStillValidated() throws Exception {
        params.setOverrideUuid(AbstractParams.OverrideUuid.OVERRIDE);
        givenAValidationThatFailsWhenReached();
        Element md = givenARecord();
        givenCollisionFromAnotherSource();

        aligner.processMetadata(md);

        HarvestResult result = harvestResult();
        assertEquals("A non-SKIP collision must still be validated and, being invalid, rejected",
            1, result.doesNotValidate);
        assertEquals("A record rejected by validation must not be skipped",
            0, result.uuidSkipped);
    }

    private Element givenARecord() throws Exception {
        Element md = new Element("root");
        when(metadataSchemaUtils.autodetectSchema(eq(md), any())).thenReturn(DETECTED_SCHEMA);
        when(metadataUtils.extractUUID(DETECTED_SCHEMA, md)).thenReturn(UUID);
        return md;
    }

    private void givenCollisionFromAnotherSource() throws Exception {
        // Record exists in the catalogue (EXISTING_ID) but does not belong to this harvester.
        when(metadataUtils.getMetadataId(UUID)).thenReturn(EXISTING_ID);
        setLocalUuids(uuidMapperReturning(UUID, null));
    }

    /**
     * Configures XSD validation backed by a {@link SchemaManager} that always throws, so validation
     * fails whenever it is actually reached. The tests assert on the resulting counters to prove
     * whether validation ran.
     */
    private void givenAValidationThatFailsWhenReached() {
        params.setValidate(HarvestValidationEnum.XSDVALIDATION);
        SchemaManager schemaManager = mock(SchemaManager.class);
        when(schemaManager.autodetectSchema(any())).thenThrow(new IllegalStateException("invalid metadata"));
        when(context.getBean(SchemaManager.class)).thenReturn(schemaManager);
    }

    private void setLocalUuids(UUIDMapper mapper) throws Exception {
        Field field = DatabaseHarvesterAligner.class.getDeclaredField("localUuids");
        field.setAccessible(true);
        field.set(aligner, mapper);
    }

    private HarvestResult harvestResult() throws Exception {
        Field field = DatabaseHarvesterAligner.class.getDeclaredField("result");
        field.setAccessible(true);
        return (HarvestResult) field.get(aligner);
    }

    private UUIDMapper uuidMapperReturning(String uuid, String id) {
        UUIDMapper mapper = mock(UUIDMapper.class);
        when(mapper.getID(uuid)).thenReturn(id);
        return mapper;
    }
}
