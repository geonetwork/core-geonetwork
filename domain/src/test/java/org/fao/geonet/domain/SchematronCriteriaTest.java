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

import static org.junit.Assert.*;

import org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest;
import org.fao.geonet.repository.SchematronCriteriaRepositoryTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test schematron criteria class Created by Jesse on 2/28/14.
 */
public class SchematronCriteriaTest {

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        criteria.setValue("");
        final Element xml = criteria.asXml();
        assertEquals("''", xml.getChildText("value"));
    }

    @Test
    public void testAsNullUiPropertiesDefaultToNormalValues() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        criteria.setUiType(null);
        criteria.setUiValue(null);
        final Element xml = criteria.asXml();
        assertEquals(criteria.getType().toString(), xml.getChildText(SchematronCriteria.EL_UI_TYPE));
        assertEquals(criteria.getValue(), xml.getChildText(SchematronCriteria.EL_UI_VALUE));
    }

    @Test
    public void testCopy() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        final SchematronCriteria copy = criteria.copy();

        Element expectedXml = criteria.asXml();
        expectedXml.removeChild("id");

        Element actualXml = copy.asXml();
        actualXml.removeChild("id");
        assertEquals(Xml.getString(expectedXml), Xml.getString(actualXml));

    }
}
