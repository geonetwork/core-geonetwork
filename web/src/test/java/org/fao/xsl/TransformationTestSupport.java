//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.xsl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import jeeves.utils.Xml;

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Validator;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaLoader;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03;
import org.fao.geonet.services.gm03.TranslateAndValidate;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03Base.FlattenerException;
import org.fao.geonet.util.ISODate;
import org.hamcrest.core.IsInstanceOf;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.filter.Filter;
import org.xml.sax.SAXException;

/**
 * Methods that support transformation and validation tests
 *
 * @author jeichar
 */
public final class TransformationTestSupport {

    static {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    }
    static File transformGM03_2toIso( File src, File outputDir ) throws Exception {
        return transformGM03_2toIso(src, outputDir, true);
    }
    static File transformGM03_2toIso( File src, File outputDir, boolean testValidity ) throws Exception {
        try {
            TranslateAndValidate transformer = new TranslateAndValidate();
            transformer.outputDir = outputDir;
            transformer.debug = true;
            transformer.run(new File(TransformationTestSupport.geonetworkWebapp, "xsl/conversion/import/GM03_2-to-ISO19139CHE.xsl"),
                    TransformationTestSupport.isoXsd, new String[]{src.getAbsolutePath()});
        } catch (AssertionError e) {
            if (testValidity) throw e;
        }
        return new File(outputDir, "result_" + src.getName());
    }

    static File transformIsoToGM03( File src, File outputDir ) throws SAXException, TransformerConfigurationException, FlattenerException,
            IOException, TransformerException {
        return transformIsoToGM03(src, outputDir, true);
    }
    static File transformIsoToGM03( File src, File outputDir, boolean testValidity ) throws SAXException,
            TransformerConfigurationException, FlattenerException, IOException, TransformerException {
        try {
            ISO19139CHEtoGM03 otherway = new ISO19139CHEtoGM03(TransformationTestSupport.gm03Xsd,
                    TransformationTestSupport.toGm03StyleSheet.getAbsolutePath());
            otherway.convert(src.getAbsolutePath(), "TransformationTestSupport");
        } catch (AssertionError e) {
            if (testValidity) throw e;
        } catch (RuntimeException e) {
            if (testValidity) throw e;
        }

        return new File(outputDir, "result_" + src.getName());

    }

    static File copy( File in, File out ) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return out;
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

    public static void delete( File current ) {
        if (current.isDirectory()) {
            for( File file : current.listFiles() ) {
                delete(file);
            }
        }
        current.delete();
    }

    private static File findWebappDir( File dir ) {
        File webappDir = new File(dir, "src/main/webapp");
        if (webappDir.exists()) {
            return webappDir;
        }
        return findWebappDir(dir.getParentFile());
    }
    public static final File data = new File(TransformationTestSupport.class.getResource("/data").getFile()).getAbsoluteFile();
    public static final File outputDir = new File(data.getParentFile(), "output").getAbsoluteFile();
    public static final File geonetworkWebapp = findWebappDir(data);
    public static final File toIsoStyleSheet = new File(geonetworkWebapp, "xsl/conversion/import/GM03-to-ISO19139CHE.xsl");
    public static final File toGm03StyleSheet = new File(geonetworkWebapp, "xsl/conversion/import/ISO19139CHE-to-GM03.xsl");
    public static final File gm03Xsd = new File(geonetworkWebapp, "WEB-INF/data/config/schema_plugins/iso19139.che/GM03_2.xsd");
    public static final File isoXsd = new File(geonetworkWebapp, "WEB-INF/data/config/schema_plugins/iso19139.che/schema.xsd");

    public static File transformGM03_1ToIso( File src, File outputDir ) throws Exception {
        return transformGM03_1ToIso(src, outputDir, true);
    }
    public static File transformGM03_1ToIso( File src, File outputDir, boolean testValidity ) throws Exception {

        try {
            TranslateAndValidate transformer = new TranslateAndValidate();
            transformer.outputDir = outputDir;
            transformer.run(toIsoStyleSheet, isoXsd, new String[]{src.getAbsolutePath()});
        } catch (AssertionError e) {
            if (testValidity) {
                throw e;
            }
        }
        return new File(outputDir, "result_" + src.getName());
    }

    public static Element transform( Class root, String pathToXsl, String testData ) throws IOException, JDOMException, Exception {
        Element xml = getXML(root, testData);
        String sSheet = new File(pathToXsl).getAbsolutePath();
        Element transform = Xml.transform(xml, sSheet);
        return transform;
    }

    public static Element getXML( Class root, String name ) throws IOException, JDOMException {
        if (root == null) root = TransformationTestSupport.class;
        InputStream xmlsource = root.getResourceAsStream(name);
        return Xml.loadStream(xmlsource);
    }

    static Validator VALIDATOR = new Validator(outputDir + "/htmlCache");

    public static void schematronValidation( File metadataFile ) throws Exception {

        Element metadata = Xml.loadFile(metadataFile);
        String chePath = "/WEB-INF/data/config/schema_plugins/iso19139.che/";

        Element env = new Element("env");

        env.addContent(new Element("id").setText("RandomID"));
        env.addContent(new Element("uuid").setText(UUID.randomUUID().toString()));
        env.addContent(new Element("changeDate").setText(new ISODate().toString()));
        env.addContent(new Element("siteURL").setText("http://localhost:8080/geonetwork/srv/eng"));

        // --- setup root element

        Element root = new Element("root");
        root.addContent(metadata);
        root.addContent(env);

        String fixedDataSheet = geonetworkWebapp + chePath + Geonet.File.UPDATE_FIXED_INFO;
        root = Xml.transform(root, fixedDataSheet);
        String path = geonetworkWebapp + chePath;

        String name = "iso19139.che";
        String xmlSchemaFile = geonetworkWebapp + chePath + Geonet.File.SCHEMA;
        String xmlSubstitutionsFile = geonetworkWebapp + chePath + Geonet.File.SCHEMA_SUBSTITUTES;;

        MetadataSchema mds = new SchemaLoader().load(xmlSchemaFile, xmlSubstitutionsFile);
        mds.setName(name);
        mds.setSchemaDir(path);
        mds.loadSchematronRules(geonetworkWebapp.getAbsolutePath());

        Element schemaTronXml = VALIDATOR.getSchemaTronXmlReport(mds, root, Geonet.DEFAULT_LANGUAGE);

        List<Element> schematronReport = schemaTronXml.getChildren("report", Edit.NAMESPACE);

        StringBuilder errors = new StringBuilder();

        // --- For each schematron report (ISO, INSPIRE, GM03) create an index field
        for( Element schematronerrors : schematronReport ) {

            if (schematronerrors.getChildren("errorFound", Edit.NAMESPACE).size() != 0) {
                for( Element element : (List<Element>) schematronerrors.getDescendants() ) {
                    if (element.getName().equals("failed-assert")) {
                        String patternName = element.getChild("pattern", Edit.NAMESPACE).getAttributeValue("name");
                        Iterator<Text> diagTxt = element.getChild("diagnostics", Edit.NAMESPACE).getDescendants(new Filter(){

                            public boolean matches( Object arg0 ) {

                                return (arg0 instanceof Text);
                            }
                        });

                        StringBuilder diagnostics = new StringBuilder();
                        while( diagTxt.hasNext() ) {
                            diagnostics.append(diagTxt.next().getTextTrim());
                        }
                        if (!diagnostics.toString().startsWith("INSPIRE")) {
                            errors.append("name=" + patternName + "\n" + diagnostics);
                            errors.append("\n");
                        }
                    }
                }
            }

        }

        assertTrue(metadataFile + " is not valid: \n" + errors.toString(), errors.length() == 0);

    }

    public static File file( String fileName ) {
        Class<TransformationTestSupport> root = TransformationTestSupport.class;
        return new File(root.getResource(fileName).getFile());

    }
}
