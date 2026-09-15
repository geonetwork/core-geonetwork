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

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;

import static org.fao.geonet.schema.TestSupport.getResource;
import static org.fao.geonet.schema.TestSupport.getResourceInsideSchema;

public class StacConversionTest {

	private static final boolean GENERATE_EXPECTED_FILE = false;

	@BeforeClass
	public static void initSaxon() {
		TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
	}

	@Test
	public void sentinel2RadiometricIndices() throws Exception {
		transformAndCompare("convert/stac-to-iso19115-3.xsl", "sentinel-2-radiometric-indices-input.xml", "sentinel-2-radiometric-indices-output.xml");
	}

	@Test
	public void biomassAnWood() throws Exception {
		transformAndCompare("convert/stac-to-iso19115-3.xsl", "biomass-and-wood-input.xml", "biomass-and-wood-output.xml");
	}

	@Test
	public void sigma0Orthorectified() throws Exception {
		transformAndCompare("convert/stac-to-iso19115-3.xsl", "sigma0-orthorectified-input.xml", "sigma0-orthorectified-output.xml");
	}


	private void transformAndCompare(String scriptName, String inputFileName, String expectedFileName) throws Exception {
		Path xslFile = getResourceInsideSchema(scriptName);
		Path xmlFile = getResource("stacHarvester/" + inputFileName);
		Element md = Xml.loadFile(xmlFile);

		Element mdIso19115_3 = Xml.transform(md, xslFile);

		XPath xPath = XPath.newInstance(".//mdb:dateInfo/cit:CI_Date/cit:date/gco:DateTime");
		((Element)xPath.selectNodes(mdIso19115_3).get(0)).setText("2025-10-23T13:41:06.565+02:00");

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
		String actual = xmlOutputter.outputString(new Document(mdIso19115_3));
		TestSupport.assertGeneratedDataByteMatchExpected("stacHarvester/" + expectedFileName, actual, GENERATE_EXPECTED_FILE);
	}

}
