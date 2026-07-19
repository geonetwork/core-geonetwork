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

import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.fao.geonet.schema.TestSupport.getResource;
import static org.fao.geonet.schema.TestSupport.getResourceInsideSchema;

public class SchematronTest {
	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();
	private static final boolean GENERATE_EXPECTED_FILE = false;
	private static Path compiledSchematronFilePath;

	@BeforeClass
	public static void makeUtilsFnAvailable() throws IOException, URISyntaxException {
		Path targetUtilsFnFile = temporaryFolder.getRoot().toPath().resolve("xsl/utils-fn.xsl");
		Files.createDirectories(targetUtilsFnFile.getParent());
		IO.copyDirectoryOrFile( getResource("gn-site/xsl/utils-fn.xsl"), targetUtilsFnFile, false);
	}

	@BeforeClass
	public static void initSaxonAndCompileSchematron() throws Exception {
		TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
		Element schematronSource = Xml.loadFile(getResourceInsideSchema("schematron/schematron-rules-iso.sch"));
		Path schematronCompilation = getResource("gn-site/WEB-INF/classes/schematron/iso_svrl_for_xslt2.xsl");
		Element compiledSchematron = Xml.transform(schematronSource, schematronCompilation);
		compiledSchematronFilePath = temporaryFolder.getRoot().toPath().resolve("path/requiredtoFind/utilsfile/compiled-iso-schematron.xsl");
		Files.createDirectories(compiledSchematronFilePath.getParent());
		Files.write(compiledSchematronFilePath, Xml.getString(compiledSchematron).getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void isoSchematronTest() throws Exception {
		applySchematronAndCompare( "UpperRhineCastles-iso19115-3.2018.xml", "UpperRhineCastles-schematron-rules-iso-report.xml");
	}

	@Test
	public void createFreValidationReport() throws Exception {
		createValidationReport("fre",
				"UpperRhineCastles-schematron-rules-iso-report.xml",
				"UpperRhineCastles-validation-report.xml");
	}

	@Test
	public void createGerValidationReport() throws Exception {
		createValidationReport("ger",
				"UpperRhineCastles-schematron-rules-iso-report-with-ia-based-german.xml",
				"UpperRhineCastles-validation-report-with-ia-based-german.xml");
	}
	private void createValidationReport(String languageCode, String sourceFile, String controlFile) throws Exception {
		Path xslFile = getResource("gn-site/xslt/services/metadata/validate.xsl");

		org.jdom.Namespace geonetNs = org.jdom.Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork");
		Element root = new Element("root");
		Element language = new Element("language");
		language.setText(languageCode);
		Element rootReport = new Element("report", geonetNs);
		Element schematronErrors = new Element("schematronerrors", geonetNs);
		Element report = new Element("report", geonetNs);
		Path xmlFile = getResourceInsideSchema(sourceFile);

		Element source = Xml.loadFile(xmlFile);

		root.addContent(rootReport);
		root.addContent(language);
		rootReport.addContent(schematronErrors);
		schematronErrors.addContent(report);
		report.addContent(source);

		Element transformed = Xml.transform(root, xslFile);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
		String actual = xmlOutputter.outputString(new Document(transformed));
		TestSupport.assertGeneratedDataByteMatchExpected(controlFile, actual, GENERATE_EXPECTED_FILE);
	}

	private void applySchematronAndCompare(String inputFileName, String expectedFileName) throws Exception {
		Path xmlFile = getResource( inputFileName);
		Element md = Xml.loadFile(xmlFile);

		Element report = Xml.transform(md, compiledSchematronFilePath, Map.of(
				"rule", "schematron-rules-iso",
				"thesaurusDir", getResource("gn-site/WEB-INF/data/config/codelist").toAbsolutePath().toString(),
				"lang", "fre"
		));

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
		String actual = xmlOutputter.outputString(new Document(report));
		TestSupport.assertGeneratedDataByteMatchExpected(expectedFileName, actual, GENERATE_EXPECTED_FILE);
	}

}
