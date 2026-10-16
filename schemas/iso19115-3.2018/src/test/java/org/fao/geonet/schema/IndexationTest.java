/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import com.google.common.collect.Lists;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018Namespaces;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.ResolverWrapper;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import static org.fao.geonet.schema.TestSupport.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IndexationTest {

	private static final ArrayList<Namespace> NAMESPACES =
			Lists.newArrayList(ISO19115_3_2018Namespaces.MDB, ISO19115_3_2018Namespaces.CIT, ISO19115_3_2018Namespaces.GCO);

	private static Field resolverMapField;
	private static final boolean GENERATE_EXPECTED_FILE = false;
	private static TimeZone timeZoneToReset;

	@BeforeClass
	public static void initOasis() throws NoSuchFieldException, IllegalAccessException, URISyntaxException {
		resolverMapField = ResolverWrapper.class.getDeclaredField("resolverMap");
		resolverMapField.setAccessible(true);
		((Map<?, ?>) resolverMapField.get(null)).clear();
		ResolverWrapper.createResolverForSchema("DEFAULT", Path.of(IndexationTest.class.getClassLoader().getResource("gn-site/WEB-INF/oasis-catalog.xml").getPath()));
	}

	@Before
	public void initSaxon() {
		TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
	}

	@AfterClass
	public static void clearOasis() throws IllegalAccessException {
		((Map<?,?>) resolverMapField.get(null)).clear();
	}

	@BeforeClass
	public static void setTimeZoneUtc() {
		timeZoneToReset = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

	@BeforeClass
	public static void resetTimeZone() {
		TimeZone.setDefault(timeZoneToReset);
	}

	@Test
	public void upperRhineCastles() throws Exception {
		XslUtil.IS_INSPIRE_ENABLED = false;
		transformAndCompare("gn-site/WEB-INF/data/config/schema_plugins/iso19115-3.2018/index-fields/index.xsl",  "UpperRhineCastles-iso19115-3.2018.xml", "UpperRhineCastles-index.xml");
	}

	@Test
	public void indexXmlCreationDateOrNot() throws Exception {
		Path xslFile = getResource("gn-site/WEB-INF/data/config/schema_plugins/iso19115-3.2018/index-fields/index.xsl");
		Path xmlFile = getResource("UpperRhineCastles-iso19115-3.2018.xml");
		Element md = Xml.loadFile(xmlFile);
		Element mdRevisionDate = Xml.selectElement(md, "mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'revision']", NAMESPACES);
		Element mdCreationDate = (Element)mdRevisionDate.clone();
		Xml.selectElement(mdCreationDate, "cit:CI_Date/cit:date/gco:DateTime", NAMESPACES).setText("2024-01-01T02:30:00Z");
		Xml.selectElement(mdCreationDate, "cit:CI_Date/cit:dateType/cit:CI_DateTypeCode", NAMESPACES).setAttribute("codeListValue", "creation").setText("creation");
		md.addContent(md.indexOf(mdRevisionDate), mdCreationDate);

		XslUtil.READ_CREATE_DATE_FROM_XML = true;
		Element index = Xml.transform(md, xslFile);

		assertEquals("2024-01-01T02:30:00.000Z", Xml.selectElement(index, "createDate", NAMESPACES).getText());

		XslUtil.READ_CREATE_DATE_FROM_XML = false;
		index = Xml.transform(md, xslFile);

		assertNull(Xml.selectElement(index, "createDate", NAMESPACES));
	}

	private void transformAndCompare(String scriptName, String inputFileName, String expectedFileName) throws Exception {
		Path xslFile = getResource(scriptName);
		Path xmlFile = getResource(inputFileName);
		Element md = Xml.loadFile(xmlFile);

		Element mdIso19115_3 = Xml.transform(md, xslFile);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
		String actual = xmlOutputter.outputString(new Document(mdIso19115_3))
				.replaceAll("<indexingDate>.*</indexingDate>", "<indexingDate>2025-04-11T17:46:21+02:00</indexingDate>");;
		TestSupport.assertGeneratedDataByteMatchExpected(expectedFileName, actual, GENERATE_EXPECTED_FILE);
	}
}
