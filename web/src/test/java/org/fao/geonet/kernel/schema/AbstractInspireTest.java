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

package org.fao.geonet.kernel.schema;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * Test the inspire schematron.
 *
 * Created by Jesse on 1/31/14.
 */
public abstract class AbstractInspireTest extends AbstractSchematronTest {
    protected static final String INSPIRE_VALID_ISO19139_XML = "inspire-valid-iso19139.xml";

    protected abstract Path getSchematronXsl();


    @Test
    public void testValid() throws Exception {
        final Element validMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        Element results = Xml.transform(validMetadata, getSchematronXsl(), params);
        assertEquals(0, countFailures(results));
    }

}
