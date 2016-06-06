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

package org.fao.geonet.util.xml;

import static org.fao.geonet.Assert.*;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

import java.util.List;

/**
 * Unit test for NamespaceUtils.
 *
 * @author heikki doeleman
 */
public class NamespaceUtilsTest {

    @Test
    public void testNamespaceInScope() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
            "xmlns:a=\"http://aaa\" " +
            "xmlns:b=\"http://bbb\" " +
            "xmlns:c=\"http://ccc\" " +
            "xmlns:d=\"http://ddd\">" +
            "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> inScope = NamespaceUtils.getNamespacesInScope(xmle);

        assertContains("Expected inscope namespace " + Namespace.NO_NAMESPACE, Namespace.NO_NAMESPACE, inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("a", "http://aaa"), Namespace.getNamespace("a", "http://aaa"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), inScope);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        inScope = NamespaceUtils.getNamespacesInScope(a);

        assertContains("Expected inscope namespace " + Namespace.NO_NAMESPACE, Namespace.NO_NAMESPACE, inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("a", "http://aaa"), Namespace.getNamespace("a", "http://aaa"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), inScope);
    }

    public void testNamespaceInherited() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
            "xmlns:a=\"http://aaa\" " +
            "xmlns:b=\"http://bbb\" " +
            "xmlns:c=\"http://ccc\" " +
            "xmlns:d=\"http://ddd\">" +
            "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> inHerited = NamespaceUtils.getNamespacesInherited(xmle);

        assertContains("Expected inherited namespace " + Namespace.NO_NAMESPACE, Namespace.NO_NAMESPACE, inHerited);
        assertContains("Expected inherited namespace " + Namespace.XML_NAMESPACE, Namespace.XML_NAMESPACE, inHerited);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        inHerited = NamespaceUtils.getNamespacesInherited(a);

        assertContains("Expected inherited namespace " + Namespace.NO_NAMESPACE, Namespace.NO_NAMESPACE, inHerited);
        assertContains("Expected inherited namespace " + Namespace.XML_NAMESPACE, Namespace.XML_NAMESPACE, inHerited);
    }

    public void testNamespaceIntroduced() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
            "xmlns:a=\"http://aaa\" " +
            "xmlns:b=\"http://bbb\" " +
            "xmlns:c=\"http://ccc\" " +
            "xmlns:d=\"http://ddd\">" +
            "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> introduced = NamespaceUtils.getNamespacesIntroduced(xmle);

        assertContains("Expected introduced namespace " + Namespace.getNamespace("a", "http://aaa"), Namespace.getNamespace("a", "http://aaa"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), introduced);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        introduced = NamespaceUtils.getNamespacesIntroduced(a);

        assertEquals("Expected 0 introduced namespaces", 0, introduced.size());

    }
}
