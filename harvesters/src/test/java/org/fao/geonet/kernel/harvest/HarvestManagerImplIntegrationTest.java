/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest;

import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.harvest.harvester.csw.CswHarvesterIntegrationTest;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test the harvest manager.
 *
 * User: Jesse Date: 10/24/13 Time: 2:27 PM
 */
public class HarvestManagerImplIntegrationTest extends AbstractHarvesterServiceIntegrationTest {
    @Autowired
    SourceRepository _sourceRepository;
    @Autowired
    HarvesterSettingRepository _settingsRepo;
    @Autowired
    HarvesterSettingsManager _settingsManager;
    @Autowired
    MetadataRepository _metadataRepository;
    @Autowired
    HarvestHistoryRepository _harvestHistoryRepository;

    @PersistenceContext
    EntityManager em;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAddRemove() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element cswHarvesterParams = createHarvesterParams("csw");
        CswHarvesterIntegrationTest.addCswSpecificParams(cswHarvesterParams, CswHarvesterIntegrationTest.OUTPUT_SCHEMA);

        final String harvesterId = _harvestManager.addHarvesterReturnId(cswHarvesterParams, serviceContext.getUserSession().getUserId());

        final Element harvesterParamsAfterAdd = _harvestManager.get(harvesterId, serviceContext, "date");

        assertNotNull(harvesterParamsAfterAdd);
        assertEquals(harvesterId, harvesterParamsAfterAdd.getAttributeValue("id"));

        String harvesterUUID = Xml.selectString(harvesterParamsAfterAdd, "site/uuid");
        assertNotNull(harvesterUUID);
        assertTrue(_sourceRepository.exists(harvesterUUID));
        assertTrue(existsInSettingsRepository(harvesterId));
        assertTrue(existsInSettingsManager(harvesterId));

        addMetadata(harvesterUUID);
        assertEquals(1, _metadataRepository.count());

        addHarvestHistory(harvesterUUID);
        assertEquals(1, _harvestHistoryRepository.count());

        final Common.OperResult result = _harvestManager.remove(harvesterId);

        assertEquals(Common.OperResult.OK, result);
        assertEquals("There is still a metadata in the repository", 0, _metadataRepository.count());
        assertEquals(1, _harvestHistoryRepository.count());
        assertTrue(_harvestHistoryRepository.findAll().get(0).isDeleted());
        assertFalse(_sourceRepository.exists(harvesterUUID));
        assertFalse(existsInSettingsRepository(harvesterId));
        assertFalse(existsInSettingsManager(harvesterId));

        final Element harvesterParamsAfterRemove = _harvestManager.get(harvesterId, serviceContext, "date");

        assertNull(harvesterParamsAfterRemove);

    }

    private void addHarvestHistory(String harvesterUUID) {
        final HarvestHistory harvestHistory = new HarvestHistory().setDeleted(false).setElapsedTime(1234).setHarvesterName("name")
            .setHarvesterType("csw").setHarvesterUuid(harvesterUUID);

        _harvestHistoryRepository.save(harvestHistory);

    }

    private void addMetadata(String harvesterUUID) {
        final Metadata entity = MetadataRepositoryTest.newMetadata(_inc);
        entity.getHarvestInfo().setHarvested(true);
        entity.getHarvestInfo().setUuid(harvesterUUID);
        _metadataRepository.save(entity);
    }

    private boolean existsInSettingsManager(String harvesterId) {
        final Element element = _settingsManager.get("harvester/id:" + harvesterId, 1);
        return element != null;
    }

    private boolean existsInSettingsRepository(String harvesterId) {
        List<HarvesterSetting> found = _settingsRepo.findAllByPath("harvester/id:" + harvesterId);
        return !found.isEmpty();
    }
}
