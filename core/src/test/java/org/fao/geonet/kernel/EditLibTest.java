package org.fao.geonet.kernel;

import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.Assert.*;
import static org.fao.geonet.Assert.assertEqualsText;
import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;

/**
 * Created by Jesse on 12/6/13.
 */
public class EditLibTest {

    public static TemporaryFolder _schemaCatalogContainer = new TemporaryFolder();
    private static SchemaManager _schemaManager;

    @BeforeClass
    public static void initSchemaManager() throws Exception {
        _schemaCatalogContainer.create();
        final GeonetworkDataDirectory gdd = new GeonetworkDataDirectory();

        final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
        gdd.init("geonetwork", AbstractCoreIntegrationTest.getWebappDir(), serviceConfig, null);

        String iso19139SchemaDir = gdd.getSchemaPluginsDir() + "/iso19139";

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

        SchemaManager manager = new SchemaManager();
        final String resourcePath = gdd.getResourcesDir().getAbsolutePath();
        final String basePath = gdd.getWebappDir();
        final String schemaPluginsCat = _schemaCatalogContainer.getRoot() + "/" + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        final String schemaPluginsDir = gdd.getSchemaPluginsDir().getAbsolutePath();

        SchemaManager.registerXmlCatalogFiles(gdd.getWebappDir(), schemaPluginsCat);

        new File(schemaPluginsDir).mkdirs();
//        IO.copyDir(new File(iso19139SchemaDir), new File(schemaPluginsDir, "iso19139"));
        manager.configure(basePath, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng", "iso19139", false);
        _schemaManager = manager;
    }

    @AfterClass
    public static void cleanUpSchemaCatalogFile() {
        _schemaCatalogContainer.delete();
        _schemaManager = null;
    }

    @Test
    public void testAddElementFromXpath_NoAttributes() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "newValue";
        final String xpathProperty = "gmd:fileIdentifier/gco:CharacterString";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testEditElementFromXpath_NoAttributes() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(
                new Element("fileIdentifier", GMD).addContent(
                        new Element("CharacterString", GCO).setText("OldValue")
                )
        );

        String newValue = "newValue";
        final String xpathProperty = "gmd:fileIdentifier/gco:CharacterString";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testReplaceFragmentFromXpath_NoAttributes() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
                createReferenceSystemInfo(code1),
                createReferenceSystemInfo(code2),
                createReferenceSystemInfo(code3)
        ));

        String newValue = "newValue";
        String newRefSystem = Xml.getString(createReferenceSystemInfo(newValue).getChild("ReferenceSystem", GMD));
        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, refSysElemName+"[2]", newRefSystem, true);


        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";
        assertEqualsText(code1, metadataElement, refSysElemName + "[1]"+xpath, GMD, GCO);
        assertEqualsText(code3, metadataElement, refSysElemName + "[3]"+xpath, GMD, GCO);
        assertEqualsText(newValue, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }

    private Element createReferenceSystemInfo(String code1) {
        return new Element("referenceSystemInfo", GMD).addContent(
                new Element("ReferenceSystem", GMD).addContent(
                        new Element("referenceSystemIdentifier", GMD).addContent(
                                new Element("RS_Identifier", GMD).addContent(
                                        new Element("code", GMD).addContent(
                                                new Element("CharacterString", GCO).setText(code1)
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void testAddFragmentFromXpath_NoAttributes() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String name = "Individual Name";
        String newValue =
                "<gmd:CI_ResponsibleParty  xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                ".org/2005/gco\">" +
                "<gmd:individualName>" +
                "<gco:CharacterString>" + name + "</gco:CharacterString>" +
                "</gmd:individualName>" +
                "</gmd:CI_ResponsibleParty>";
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement, schema,
                "gmd:contact/gmd:CI_ResponsibleParty", newValue, true);

        assertEqualsText(name, metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString", GMD, GCO);
        final List<Namespace> nsList = Arrays.asList(GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString",
                nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact", nsList).size());
    }

    @Test
    public void testAddElementFromXpath_AttributeInPath() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String newValue = "newValue";
        final String xpathProperty = "gmd:fileIdentifier[@nilReason = 'withheld']/gco:CharacterString";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
        assertEquals("withheld", Xml.selectString(metadataElement, "gmd:fileIdentifier/@nilReason", Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testAddAttributeFromXpath_AttributeInPath() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "service";
        final String xpathProperty = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEquals(newValue, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel/gmd:MD_ScopeCode", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel", Arrays.asList(GMD, GCO)).size());

        String newValue2 = "dataset";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue2, true);

        assertEquals(newValue2, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel/gmd:MD_ScopeCode", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel", Arrays.asList(GMD, GCO)).size());
    }

    @Test (expected = AssertionError.class)
    public void testAddAttribute_ValueIsNode1() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "<service>xml is not allowed here</service>";
        final String xpathProperty = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);
    }
    @Test (expected = AssertionError.class)
    public void testAddAttribute_ValueIsNode2() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "<service>xml is not allowed here</service>";
        final String xpathProperty = "@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);
    }

    public void testAddElement_ValueIsNode() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "<gco:CharacterString>xml</gco:CharacterString>";
        final String xpathProperty = "gmd:fileIdentifier[@nilReason='withheld']";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals("withheld", Xml.selectString(metadataElement, "gmd:fileIdentifier/@nilReason", Arrays.asList(GMD, GCO)));
    }

}
