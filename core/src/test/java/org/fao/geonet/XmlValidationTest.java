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

package org.fao.geonet;

import jeeves.xlink.XLink;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test that metadata can be correctly validated against their schema.
 *
 * @author Jesse on 11/13/2014.
 */
public class XmlValidationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private DataManager manager;

    @Test
    public void testBasicValidation() throws Exception {
        Element metadata = Xml.loadFile(XmlValidationTest.class.getResource("kernel/valid-metadata.iso19139.xml"));
        removeHrefs(metadata);
        assertTrue(manager.validate(metadata));
        metadata = Xml.loadFile(XmlValidationTest.class.getResource("kernel/search/DE_Search_MD.iso19139.xml"));
        assertFalse(manager.validate(metadata));
    }

    protected void removeHrefs(Element metadata) {
        final Iterator descendants = metadata.getDescendants();
        while (descendants.hasNext()) {
            Object next = descendants.next();
            if (next instanceof Element) {
                Element element = (Element) next;
                element.removeAttribute(XLink.HREF, XLink.NAMESPACE_XLINK);
            }
        }
    }
}
