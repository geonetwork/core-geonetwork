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

package org.fao.geonet.domain;

import org.fao.geonet.repository.AbstractOperationsAllowedTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OperationAllowedTest extends AbstractOperationsAllowedTest {
    @Test
    public void testGetGroup() {
        OperationAllowed operationAllowed = _opAllowRepo.findOne(_opAllowed1.getId());
        assertNotNull(operationAllowed);
        assertEquals(_opAllowed1.getId().getGroupId(), operationAllowed.getId().getGroupId());
    }

    @Test
    public void testConstructors() {
        int mdId = 1;
        int opId = 20;
        int grpId = 300;
        assertEquals(new OperationAllowedId().setMetadataId(mdId).setGroupId(grpId).setOperationId(opId),
            new OperationAllowedId(mdId, grpId, opId));
    }

}
