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

package org.fao.geonet.services.harvesting;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.kernel.harvest.AbstractHarvesterServiceIntegrationTest;
import org.fao.geonet.kernel.harvest.harvester.csw.CswHarvesterIntegrationTest;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.fao.geonet.repository.specification.HarvestHistorySpecs.hasHarvesterUuid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test adding a harvester.
 *
 * User: Jesse Date: 10/23/13 Time: 11:41 AM
 */
public class AddIntegrationTest extends AbstractHarvesterServiceIntegrationTest {
    @Autowired
    private HarvesterSettingsManager _settingsManager;
    @Autowired
    private HarvestHistoryRepository _historyRepository;

    @Test
    public void testExec() throws Exception {
        final Element beforeAddSettings = _settingsManager.get("harvesting", -1);
        assertEquals(0, beforeAddSettings.getChildren("children").size());

        final Add add = new Add();

        Element params = createHarvesterParams("csw");
        CswHarvesterIntegrationTest.addCswSpecificParams(params, CswHarvesterIntegrationTest.OUTPUT_SCHEMA);
        final ServiceContext serviceContext = createServiceContext();

        final Element response = add.exec(params, serviceContext);
        String id = response.getChildText("id");

        final Element afterAddSettings = _settingsManager.get("harvesting", -1);
        assertEquals(1, afterAddSettings.getChildren("children").size());

        final Element nodeSettings = _settingsManager.get("harvester/id:" + id, -1);
        assertNotNull(nodeSettings);

        final List<HarvestHistory> harvestHistoryByType = _historyRepository.findAllByHarvesterType("csw");
        assertEquals(0, harvestHistoryByType.size());

        final List<HarvestHistory> findByUUID = _historyRepository.findAll(hasHarvesterUuid(id));
        assertEquals(0, findByUUID.size());

    }


}
