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

package org.fao.geonet.kernel.harvest.harvester.localfilesystem;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests for #9432 on the local filesystem harvester: the UUID collision {@code SKIP}
 * policy is resolved <em>before</em> validation, so a record that already exists from another
 * source is counted as {@code uuidSkipped} without being validated (and therefore never ends up as
 * {@code doesNotValidate}).
 */
public class LocalFsHarvesterFileVisitorTest {

    private static final String DETECTED_SCHEMA = "iso19139";
    private static final String UUID = "uuid-1";
    private static final String EXISTING_ID = "5";
    private static final Path FILE = Paths.get("record.xml");

    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataUtils repo;
    private LocalFilesystemParams params;
    private LocalFsHarvesterFileVisitor visitor;

    @Before
    public void setUp() throws Exception {
        context = mock(ServiceContext.class);
        dataMan = mock(DataManager.class);
        repo = mock(IMetadataUtils.class);

        when(context.getBean(DataManager.class)).thenReturn(dataMan);
        when(context.getBean(IMetadataUtils.class)).thenReturn(repo);
        // Empty category/group mappers built by the visitor constructor.
        when(context.getBean(MetadataCategoryRepository.class))
            .thenReturn(mock(MetadataCategoryRepository.class));
        when(context.getBean(MetadataCategoryRepository.class).findAll()).thenReturn(new ArrayList<>());
        when(context.getBean(GroupRepository.class)).thenReturn(mock(GroupRepository.class));
        when(context.getBean(GroupRepository.class).findAll()).thenReturn(new ArrayList<>());

        params = new LocalFilesystemParams(mock(DataManager.class));
        params.setUuid("harvester-uuid");
        params.setImportXslt("none");

        LocalFilesystemHarvester harvester = mock(LocalFilesystemHarvester.class);
        when(harvester.getLogger()).thenReturn(mock(Logger.class));

        visitor = new LocalFsHarvesterFileVisitor(new AtomicBoolean(false), context, params, harvester);
    }

    @Test
    public void invalidCollidingRecordWithSkipPolicyIsSkippedNotValidated() throws Exception {
        params.setOverrideUuid(AbstractParams.OverrideUuid.SKIP);
        givenAValidationThatFailsWhenReached();
        Element md = givenARecord();
        givenCollisionFromAnotherSource();

        visitor.processXmlData(FILE, md);

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

        visitor.processXmlData(FILE, md);

        HarvestResult result = harvestResult();
        assertEquals("A non-SKIP collision must still be validated and, being invalid, rejected",
            1, result.doesNotValidate);
        assertEquals("A record rejected by validation must not be skipped",
            0, result.uuidSkipped);
    }

    private Element givenARecord() throws Exception {
        Element md = new Element("root");
        when(dataMan.autodetectSchema(eq(md), any())).thenReturn(DETECTED_SCHEMA);
        when(dataMan.extractUUID(DETECTED_SCHEMA, md)).thenReturn(UUID);
        return md;
    }

    private void givenCollisionFromAnotherSource() throws Exception {
        // Record exists in the catalogue (EXISTING_ID) but belongs to another source.
        when(dataMan.getMetadataId(UUID)).thenReturn(EXISTING_ID);
        Metadata existing = new Metadata();
        existing.getHarvestInfo().setUuid("other-source");
        when(repo.findOne(EXISTING_ID)).thenReturn(existing);
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

    private HarvestResult harvestResult() throws Exception {
        Field field = LocalFsHarvesterFileVisitor.class.getDeclaredField("result");
        field.setAccessible(true);
        return (HarvestResult) field.get(visitor);
    }
}
