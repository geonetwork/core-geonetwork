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

import org.junit.Assert;
import org.junit.Test;

import static org.fao.geonet.domain.SchematronRequirement.*;

/**
 * Created by Jesse on 2/6/14.
 */
public class SchematronRequirementTest {
    @Test
    public void testHighestRequirement() throws Exception {
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(DISABLED));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REPORT_ONLY));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REQUIRED));

        Assert.assertEquals(REQUIRED, REPORT_ONLY.highestRequirement(REQUIRED));
        Assert.assertEquals(REPORT_ONLY, REPORT_ONLY.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT_ONLY, REPORT_ONLY.highestRequirement(REPORT_ONLY));

        Assert.assertEquals(DISABLED, DISABLED.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT_ONLY, DISABLED.highestRequirement(REPORT_ONLY));
        Assert.assertEquals(REQUIRED, DISABLED.highestRequirement(REQUIRED));
    }
}
