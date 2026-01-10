/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.schema;

import org.fao.geonet.utils.PrefixUrlRewrite;
import org.fao.geonet.utils.ResolverWrapper;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.nio.NioPathAwareCatalogResolver;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

import static org.fao.geonet.schema.TestSupport.getResource;
import static org.fao.geonet.schema.TestSupport.getResourceInsideSchema;

public class FullViewTest {

    private static final boolean GENERATE_EXPECTED_FILE = false;

    private static Field resolverMapField;

    @BeforeClass
    public static void initSaxon() {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
    }

    @BeforeClass
    public static void initOasis() throws NoSuchFieldException, IllegalAccessException, URISyntaxException {
        resolverMapField = ResolverWrapper.class.getDeclaredField("resolverMap");
        resolverMapField.setAccessible(true);
        ((Map<?, ?>) resolverMapField.get(null)).clear();

        String catFiles = getResource("gn-site/WEB-INF/oasis-catalog.xml") + ";" + addRequiredSchemasAndDisableConflictingOne();
        System.setProperty("jeeves.xml.catalog.files", catFiles);
        ResolverWrapper.createResolverForSchema("DEFAULT", null);
        ResolverWrapper.getInstance().setBlankXSLFile(getResource("config/blank.xsl").toAbsolutePath().toString());
        NioPathAwareCatalogResolver.addRewriteDirective(
                    new PrefixUrlRewrite("sharedFormatterDir/",
                            getResource("gn-site/WEB-INF/data/data/formatter").toAbsolutePath() + "/"));
    }

    @AfterClass
    public static void clearOasis() throws IllegalAccessException {
        ((Map<?,?>) resolverMapField.get(null)).clear();
    }


    @Test
    public void fullView() throws Exception {
        Path xslFile = getResourceInsideSchema("formatter/xsl-view/view.xsl");
        Path xmlFile = getResource("UpperRhineCastles-ISO19115-3-full-view-form.xml");
        Element source = Xml.loadFile(xmlFile);

        Element transformed = Xml.transform(source, xslFile, Map.of("view", "advanced", "approved", "true", "root", "div"));


        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
        String actual = xmlOutputter.outputString(new Document(transformed));
        TestSupport.assertGeneratedDataByteMatchExpected("UpperRhineCastles-ISO19115-3-full-view.html", actual, GENERATE_EXPECTED_FILE);
    }

    private static Path addRequiredSchemasAndDisableConflictingOne() throws URISyntaxException {
        return getResource("config/schemaplugin-uri-catalog.xml");
    }
}
