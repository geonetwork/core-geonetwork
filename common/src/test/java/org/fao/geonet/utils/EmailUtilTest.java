//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmailUtilTest {
    @Test
    public void testEmailAddress() {
        assertEquals(true, EmailUtil.isValidEmailAddress("test@domain.com"));

        assertEquals(true, EmailUtil.isValidEmailAddress("test@example.international"));
        
        assertEquals(true, EmailUtil.isValidEmailAddress("test.user@domain.com"));

        assertEquals(true, EmailUtil.isValidEmailAddress("test.user@domain.subdomain.com"));

        assertEquals(false, EmailUtil.isValidEmailAddress("test.user"));

        assertEquals(false, EmailUtil.isValidEmailAddress("test.user@domain"));
    }
}
