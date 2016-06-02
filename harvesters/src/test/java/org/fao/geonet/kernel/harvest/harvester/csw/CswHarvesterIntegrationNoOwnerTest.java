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

package org.fao.geonet.kernel.harvest.harvester.csw;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration Test for the Csw Harvester class.
 *
 * User: Jesse Date: 10/18/13 Time: 4:01 PM
 */
public class CswHarvesterIntegrationNoOwnerTest extends CswHarvesterIntegrationTest {

    protected void customizeParams(Element params) {
        addCswSpecificParams(params, CswHarvesterIntegrationTest.OUTPUT_SCHEMA);
        params.getChild("site").getChild("ownerId").detach();
    }

    @Override
    protected void performExtraAssertions(AbstractHarvester harvester) {
        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);
        assertEquals("" + admin.getId(), harvester.getParams().getOwnerId());
    }
}
