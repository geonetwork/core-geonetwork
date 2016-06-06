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

import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Useful extensions to Junit TestCase.
 *
 * @author heikki doeleman
 */
public final class Assert extends junit.framework.TestCase {

    /**
     * Whether something is in a collection.
     */
    public static void assertContains(String msg, Object o, Collection<?> c) {
        for (Object in : c) {
            if (o.equals(in)) {
                return;
            }
        }
        fail(msg);
    }

    /**
     * Check if an element exists and if it has the expected test.
     *
     * @param expected   the expected text
     * @param xml        the xml to search
     * @param xpath      the xpath to the element to check
     * @param namespaces the namespaces required for xpath
     */
    public static void assertEqualsText(String expected, Element xml, String xpath, Namespace... namespaces) throws JDOMException {
        final List element;
        if (namespaces == null || namespaces.length == 0) {
            element = Xml.selectNodes(xml, xpath);
        } else {
            element = Xml.selectNodes(xml, xpath, Arrays.asList(namespaces));
        }
        assertEquals("Expected 1 element but found " + element.size() + "No element found at: " + xpath + " in \n" + Xml.getString(xml),
            1, element.size());
        String text;
        if (element.get(0) instanceof Element) {
            text = ((Element) element.get(0)).getTextTrim();
        } else if (element.get(0) instanceof Attribute) {
            text = ((Attribute) element.get(0)).getValue();
        } else if (element.get(0) instanceof Text) {
            text = ((Text) element.get(0)).getTextTrim();
        } else {
            fail("Handling of " + element.get(0).getClass() + " is not yet implemented");
            text = "";
        }
        assertEquals(Xml.getString(xml), expected, text);
    }

    /**
     * Just to prevent junit.framework.AssertionFailedError: No tests found in
     * org.fao.geonet.test.TestCase.
     */
    public void testPreventAssertionFailedError() {
    }

}
