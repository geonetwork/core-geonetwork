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

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test xpath evaluator.
 *
 * Created by Jesse on 2/6/14.
 */
public class XPathCriteriaEvaluatorTest {
    static final Element testMetadata;
    private static final List<Namespace> NAMESPACES = Collections.emptyList();

    static {
        try {
            testMetadata = Xml.loadString("<geonet>\n"
                + "    <general>\n"
                + "        <profiles>../../../web/geonetwork/WEB-INF/user-profiles.xml</profiles>\n"
                + "        <uploadDir>../../../data/tmp</uploadDir>\n"
                + "        <maxUploadSize>100</maxUploadSize> <!-- Size must be in megabyte (integer) -->\n"
                + "        <debug>true</debug>\n"
                + "    </general>\n"
                + "\n"
                + "    <default>\n"
                + "        <service>main.home</service>\n"
                + "        <language></language>\n"
                + "        <localized>true</localized>\n"
                + "        <contentType>text/html; charset=UTF-8</contentType>\n"
                + "    </default>\n"
                + "</geonet>", false);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private final int metadataId = -1;

    @Test
    public void testSlashSlashAtRoot() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "//debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "//general/debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));

    }

    @Test
    public void testAcceptsXPathBoolean() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "geonet/*/debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "/*/debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*/debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text() = 'true'", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text() = 'false'", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing/text() = 'false'", metadataId, testMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsXPathString() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text()", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//language/text()", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing/text()", metadataId, testMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsXPathElement() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing", metadataId, testMetadata, NAMESPACES));

        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//service[text() = 'main.home']", metadataId, testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//service[text() = 'xyz']", metadataId, testMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsXPathError() throws Exception {
        final XPathCriteriaEvaluator xPathCriteriaEvaluator = new XPathCriteriaEvaluator() {
            @Override
            protected void warn(String value, Throwable e) {
                // do nothing so that the console stays clean
            }
        };
        assertFalse(xPathCriteriaEvaluator.accepts(null, "*//debug[d = 'da", metadataId, testMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsORXPath() throws Exception {
        SchematronCriteria oneGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language/text()", "*//debug/text()");
        assertTrue(oneGoodOrXPath.accepts(null, metadataId, testMetadata, NAMESPACES));
        SchematronCriteria twoGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language", "*//debug/text()");
        assertTrue(twoGoodOrXPath.accepts(null, metadataId, testMetadata, NAMESPACES));
        SchematronCriteria oneGoodOrXPath2 = XPathCriteriaEvaluator.createOrCriteria("*//debug/text() = 'true'", "*//debug/text()");
        assertTrue(oneGoodOrXPath2.accepts(null, metadataId, testMetadata, NAMESPACES));

        SchematronCriteria noGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language/text()", "*//service[text() = 'xyz']");
        assertFalse(noGoodOrXPath.accepts(null, metadataId, testMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsANDXPath() throws Exception {
        SchematronCriteria noGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//language/text()", "*//service[text() = 'xyz']");
        assertFalse(noGoodAndXPath.accepts(null, metadataId, testMetadata, NAMESPACES));

        SchematronCriteria oneGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//debug/text()", "*//service[text() = 'xyz']");
        assertFalse(oneGoodAndXPath.accepts(null, metadataId, testMetadata, NAMESPACES));

        SchematronCriteria twoGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//debug/text()", "*//service[text() = 'main" +
            ".home']");
        assertTrue(twoGoodAndXPath.accepts(null, metadataId, testMetadata, NAMESPACES));
    }


    @Test
    public void testAcceptsCreateFromTextANDXPath() throws Exception {
        final Text debugText = (Text) Xml.selectNodes(testMetadata, "*//debug/text()").get(0);
        final Text maxUploadSizeText = (Text) Xml.selectNodes(testMetadata, "*//maxUploadSize/text()").get(0);
        SchematronCriteria criteria = XPathCriteriaEvaluator.createAndCriteria(debugText, maxUploadSizeText);
        assertTrue(criteria.accepts(null, metadataId, testMetadata, NAMESPACES));

        Element missingDebugMetadata = (Element) testMetadata.clone();
        ((Text) Xml.selectNodes(missingDebugMetadata, "*//debug/text()").get(0)).detach();
        assertFalse(criteria.accepts(null, metadataId, missingDebugMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsCreateFromTextOrXPath() throws Exception {
        final Text debugText = (Text) Xml.selectNodes(testMetadata, "*//debug/text()").get(0);
        final Text maxUploadSizeText = (Text) Xml.selectNodes(testMetadata, "*//maxUploadSize/text()").get(0);
        SchematronCriteria criteria = XPathCriteriaEvaluator.createOrCriteria(debugText, maxUploadSizeText);
        assertTrue(criteria.accepts(null, metadataId, testMetadata, NAMESPACES));

        Element missingDebugMetadata = (Element) testMetadata.clone();
        ((Text) Xml.selectNodes(missingDebugMetadata, "*//debug/text()").get(0)).detach();
        assertTrue(criteria.accepts(null, metadataId, missingDebugMetadata, NAMESPACES));

        ((Text) Xml.selectNodes(missingDebugMetadata, "*//maxUploadSize/text()").get(0)).detach();
        assertFalse(criteria.accepts(null, metadataId, missingDebugMetadata, NAMESPACES));
    }


}
