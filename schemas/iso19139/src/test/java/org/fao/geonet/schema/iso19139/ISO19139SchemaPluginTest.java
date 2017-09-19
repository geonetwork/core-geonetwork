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

package org.fao.geonet.schema.iso19139;

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.TransformerConfigurationException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * Created by fgravin on 7/31/17.
 */
public class ISO19139SchemaPluginTest {
    protected Path root;
    protected Map<String, String> ns = new HashMap<String, String>();

    @Before
    public void setup() throws TransformerConfigurationException, URISyntaxException {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        root = Paths.get(ISO19139SchemaPluginTest.class.getResource("").toURI());

        ns.put(
                ISO19139Namespaces.GMD.getPrefix(),
                ISO19139Namespaces.GMD.getURI()
        );
        ns.put(
                ISO19139Namespaces.GCO.getPrefix(),
                ISO19139Namespaces.GCO.getURI()
        );

    }

    @Test
    public void removeTranslationFromElement() throws Exception {
        Element multilingualElement = Xml.loadFile(
                root.resolve("multilingual-contact.xml"));

        ISO19139SchemaPlugin plugin = new ISO19139SchemaPlugin();
        plugin.removeTranslationFromElement(multilingualElement, Arrays.asList(new String[]{"#EN"}));

        String resultString = Xml.getString(multilingualElement);

        assertThat(
                resultString, hasXPath("count(//gmd:PT_FreeText)", equalTo("0")).withNamespaceContext(ns)
        );
        assertThat(
                resultString, hasXPath("count(//gco:CharacterString[text() = 'Name (multilingual)'])",
                        equalTo("0")).withNamespaceContext(ns)
        );
        assertThat(
                resultString, hasXPath("count(//gco:CharacterString[text() = 'en-individualname'])",
                        equalTo("1")).withNamespaceContext(ns)
        );

    }

}
