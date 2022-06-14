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

package org.fao.geonet.schemas;

import com.google.common.collect.ImmutableSet;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;

/**
 * Base class for XSL processing tests.
 *
 * The class checks that the process with no parameter do not alter the record.
 *
 * Created by francois on 26/05/16.
 */
public abstract class XslProcessTest {


    protected Map<String, String> ns = new HashMap<String, String>();
    public Map<String, String> getNs() {
        return ns;
    }

    public XslProcessTest setNs(ImmutableSet<Namespace> ns) {
        for (Namespace n : ns) {
            this.ns.put(n.getPrefix(), n.getURI());
        }
        return this;
    }

    protected Path xslFile;
    protected Path xmlFile;

    protected String xslFilename;
    protected String xmlFilename;

    public XslProcessTest() {
    }

    public XslProcessTest setXslFilename(String xslFilename) {
        this.xslFilename = xslFilename;
        return this;
    }

    public XslProcessTest setXmlFilename(String xmlFilename) {
        this.xmlFilename = xmlFilename;
        return this;
    }

    public Class testClass;

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            testClass = description.getTestClass();
        }
    };

    @Before
    public void setup() throws URISyntaxException {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

        if (xslFilename != null) {
            xslFile = Paths.get(testClass.getClassLoader().getResource(xslFilename).toURI());
        }
        if (xmlFilename != null) {
            xmlFile = Paths.get(testClass.getClassLoader().getResource(xmlFilename).toURI());
        }
    }

    public Element testMustNotAlterARecordWhenNoParameterProvided() throws Exception {
        Element controlElement = Xml.loadFile(xmlFile);
        Element inputElement = Xml.loadFile(xmlFile);


        // First, check that the process with no parameters
        // does not alter the record
        Element resultElement = Xml.transform(inputElement, xslFile);

        String resultString = Xml.getString(resultElement);
        String controlString = Xml.getString(controlElement);
        Diff diffForNoParameter = DiffBuilder
            .compare(Input.fromString(resultString))
            .withTest(Input.fromString(controlString))
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .checkForSimilar()
            .build();

        assertFalse(
            "Process does not alter the document.",
            diffForNoParameter.hasDifferences());

        return resultElement;
    }
}
