package org.fao.geonet.schema;

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;

import static org.fao.geonet.schema.TestSupport.getResource;
import static org.fao.geonet.schema.TestSupport.getResourceInsideSchema;

public class CswTest {

	private static final boolean GENERATE_EXPECTED_FILE = false;

	@BeforeClass
	public static void initSaxon() {
		TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
	}

	@Test
	public void ech0271Summary() throws Exception {
		transformAndCompare("mdb-summary.xsl", "metadata-ISO19115-3.xml", "metadata-ISO19115-3-summary.xml");
	}

	private void transformAndCompare(String scriptName, String inputFileName, String expectedFileName) throws Exception {
		Path xslFile = getResourceInsideSchema("present/csw/" + scriptName);
		Path xmlFile = getResource(inputFileName);
		Element md = Xml.loadFile(xmlFile);

		Element cswRecord = Xml.transform(md, xslFile);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
		String actual = xmlOutputter.outputString(new Document(cswRecord));
		boolean generateExpectedFileNameOnlyIfInputDiffersFromExpected = GENERATE_EXPECTED_FILE && !expectedFileName.equals(inputFileName);
		TestSupport.assertGeneratedDataByteMatchExpected(expectedFileName, actual, generateExpectedFileNameOnlyIfInputDiffersFromExpected);
	}

}
