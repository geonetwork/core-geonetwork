package org.fao.geonet.kernel;

import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.utils.TransformerFactoryFactory;
import jeeves.utils.Xml;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.resources.Resources;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;

import static org.fao.geonet.Assert.assertEquals;
import static org.fao.geonet.Assert.assertEqualsText;
import static org.fao.geonet.Assert.assertFalse;
import static org.fao.geonet.Assert.assertTrue;
import static org.fao.geonet.Assert.fail;
import static org.fao.geonet.Assert.getWebappDir;
import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;

/**
 * Test EditLib methods.
 *
 * Created by Jesse on 12/6/13.
 */
public class EditLibTest {

    public static TemporaryFolder _schemaCatalogContainer = new TemporaryFolder();
    private static SchemaManager _schemaManager;

    @BeforeClass
    public static void initSchemaManager() throws Exception {
        _schemaCatalogContainer.create();

        final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
        final String webappDir = getWebappDir(EditLibTest.class);
        new GeonetworkDataDirectory("geonetwork", webappDir, serviceConfig, null);

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        final String resourcePath = Resources.locateResourcesDir((ServletContext)null);
        final String basePath = webappDir;
        final String schemaPluginsCat = _schemaCatalogContainer.getRoot() + "/" + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        final String schemaPluginsDir = webappDir + "/WEB-INF/data/config/schema_plugins";

        FileUtils.copyFile(new File(webappDir, "WEB-INF/"+Geonet.File.SCHEMA_PLUGINS_CATALOG), new File(schemaPluginsCat));

        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCat);

        SchemaManager manager = SchemaManager.getInstance(basePath, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng", "iso19139");
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
        boolean updated = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
                new AddElemValue(newValue),
                true);

        assertTrue(updated);
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
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testAddElementFromXpath_HasPositionalIdentifier() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(
                new Element("fileIdentifier", GMD).addContent(
                        new Element("CharacterString", GCO).setText("OldValue")
                )
        );

        String newValue = "newValue";
        final String xpathProperty = "gmd:fileIdentifier/gco:CharacterString";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testEditExistingAttribute() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(
                new Element("fileIdentifier", GMD).setAttribute("nilReason", "missing", GCO).addContent(
                        new Element("CharacterString", GCO).setText("OldValue")
                )
        );

        String newValue = "withheld";
        final String xpathProperty = "gmd:fileIdentifier/@gco:nilReason";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);

        assertEquals(newValue, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:fileIdentifier", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testReplaceFragmentFromXpath_NewXmlHasDifferentName() throws Exception {

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
        Element newRefSystem = (Element) createReferenceSystemInfo(newValue).getChild("ReferenceSystem", GMD).detach();
        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, refSysElemName + "[2]",
                new AddElemValue(newRefSystem), true);


        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";
        assertEqualsText(code1, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(code3, metadataElement, refSysElemName + "[3]" + xpath, GMD, GCO);
        assertEqualsText(newValue, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testRelativePathUpdateWithBrackets() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";

        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
                createReferenceSystemInfo(code1),
                createReferenceSystemInfo(code2),
                createReferenceSystemInfo(code3)
        ));

        final List<Namespace> theNSs = Arrays.asList(GMD, GCO);
        String xpathToDetach = "*//gmd:code";
        final List<Content> list = (List<Content>) Xml.selectNodes(metadataElement, xpathToDetach, theNSs);

        for (Content content : list) {
            content.detach();
        }

        String newValue = "newValue";
        final String updateXPath = "(*//gmd:RS_Identifier)[2]/gmd:code/gco:CharacterString";
        boolean updateOccurred = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, updateXPath,
                new AddElemValue(newValue), true);

        assertTrue(updateOccurred);

        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";

        final String refSysElemName = "gmd:referenceSystemInfo";
        assertTrue(Xml.selectNodes(metadataElement, refSysElemName + "[1]" + xpath, theNSs).isEmpty());
        assertTrue(Xml.selectNodes(metadataElement, refSysElemName + "[3]" + xpath, theNSs).isEmpty());
        assertEqualsText(newValue, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", theNSs).size());
    }
    @Test
    public void testRelativePathUpdateNoBrackets() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";

        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
                createReferenceSystemInfo(code1),
                createReferenceSystemInfo(code2),
                createReferenceSystemInfo(code3)
        ));

        final List<Namespace> theNSs = Arrays.asList(GMD, GCO);
        String xpathToDetach = "*//gmd:code";
        final List<Content> list = (List<Content>) Xml.selectNodes(metadataElement, xpathToDetach, theNSs);

        for (Content content : list) {
            content.detach();
        }

        String newValue = "newValue";
        final String updateXPath = "*//gmd:RS_Identifier/gmd:code/gco:CharacterString";
        boolean updateOccurred = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, updateXPath,
                new AddElemValue(newValue), true);

        assertTrue(updateOccurred);

        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";

        final String refSysElemName = "gmd:referenceSystemInfo";
        assertEqualsText(newValue, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertTrue(Xml.selectNodes(metadataElement, refSysElemName + "[2]" + xpath, theNSs).isEmpty());
        assertTrue(Xml.selectNodes(metadataElement, refSysElemName + "[3]" + xpath, theNSs).isEmpty());
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", theNSs).size());
    }

    @Test
    public void testRelativePathUpdateNoChildFound() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String newValue = "newValue";
        final String refSysElemName = "*//gmd:RS_Identifier/gmd:code/gco:CharacterString";
        boolean updateOccurred = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, refSysElemName,
                new AddElemValue(newValue), true);

        assertFalse(updateOccurred);
    }

    @Test
    public void testReplaceFragmentFromXpath_NewXmlRootHasSameNameAsSelectedElement() throws Exception {

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
        Element newRefSystem = createReferenceSystemInfo(newValue);
        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, refSysElemName + "[2]",
                new AddElemValue(newRefSystem), true);


        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";
        assertEqualsText(code1, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(code3, metadataElement, refSysElemName + "[3]" + xpath, GMD, GCO);
        assertEqualsText(newValue, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testReplaceFragmentFromXpath_SpecialReplaceTag() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
                createReferenceSystemInfo(code1),
                createReferenceSystemInfo(code2),
                createReferenceSystemInfo(code3)
        ));

        String newValue1 = "newValue1";
        String newValue2 = "newValue2";
        Element newRefSystems = new Element(EditLib.SpecialUpdateTags.REPLACE).addContent(Arrays.asList(
                createReferenceSystemInfo(newValue1),
                createReferenceSystemInfo(newValue2)
        ));

        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, "",
                new AddElemValue(newRefSystems), true);


        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";
        assertEqualsText(newValue1, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(newValue2, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEquals(2, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }

    @Test
    public void testReplaceFragmentFromXpath_SpecialAddTag() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
                createReferenceSystemInfo(code1),
                createReferenceSystemInfo(code2),
                createReferenceSystemInfo(code3)
        ));

        String newValue1 = "newValue1";
        String newValue2 = "newValue2";
        Element newRefSystems = new Element(EditLib.SpecialUpdateTags.ADD).addContent(Arrays.asList(
                createReferenceSystemInfo(newValue1),
                createReferenceSystemInfo(newValue2)
        ));

        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, "",
                new AddElemValue(newRefSystems), true);


        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco" +
                             ":CharacterString";
        assertEqualsText(code1, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(code2, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEqualsText(code3, metadataElement, refSysElemName + "[3]" + xpath, GMD, GCO);
        assertEqualsText(newValue1, metadataElement, refSysElemName + "[4]" + xpath, GMD, GCO);
        assertEqualsText(newValue2, metadataElement, refSysElemName + "[5]" + xpath, GMD, GCO);
        assertEquals(5, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
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
    public void testAddFragmentFromXpath_AddChildrenOfElementBecauseNameIsSameAsNodeToAdd() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String name = "Individual Name";
        String newValue =
                "<gmd:CI_ResponsibleParty gco:nilReason='withheld' xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" " +
                "xmlns:gco=\"http://www.isotc211.org/2005/gco\">" +
                "<gmd:individualName>" +
                "<gco:CharacterString>" + name + "</gco:CharacterString>" +
                "</gmd:individualName>" +
                "</gmd:CI_ResponsibleParty>";
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement, schema,
                "gmd:contact/gmd:CI_ResponsibleParty", new AddElemValue(newValue), true);

        assertEqualsText(name, metadataElement, "gmd:contact/gmd:CI_ResponsibleParty[@gco:nilReason='withheld']/gmd:individualName/gco:CharacterString", GMD, GCO);
        final List<Namespace> nsList = Arrays.asList(GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString",
                nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact", nsList).size());
    }

    @Test
    public void testAddFragmentFromXpath_ElementBecauseNameIsDifferentFromNodeInXpath() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String name = "Individual Name";
        String newValue =
                "<gmd:CI_ResponsibleParty  xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" " +
                "xmlns:gco=\"http://www.isotc211.org/2005/gco\">" +
                "<gmd:individualName>" +
                "<gco:CharacterString>" + name + "</gco:CharacterString>" +
                "</gmd:individualName>" +
                "</gmd:CI_ResponsibleParty>";
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement, schema,
                "gmd:contact", new AddElemValue(newValue), true);

        assertEqualsText(name, metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString", GMD, GCO);
        final List<Namespace> nsList = Arrays.asList(GMD, GCO);
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString",
                nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact/gmd:CI_ResponsibleParty", nsList).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:contact", nsList).size());
    }

    @Test
    public void testAddAttributeFromXpath_AttributeInPath() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "service";
        final String xpathProperty = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);

        assertEquals(newValue, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel/gmd:MD_ScopeCode", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel", Arrays.asList(GMD, GCO)).size());

        String newValue2 = "dataset";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue2),
                true);

        assertEquals(newValue2, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel/gmd:MD_ScopeCode", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel", Arrays.asList(GMD, GCO)).size());
    }

    @Test(expected = AssertionError.class)
    public void testAddAttribute_ValueIsNode1() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "<service>xml is not allowed here</service>";
        final String xpathProperty = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);
    }

    @Test
    public void testErrorOnDotDot() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "x";
        final String xpathProperty = "gmd:hierarchyLevel/../@codeListValue";
        assertFalse(new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true));
    }

    @Test
    public void testErrorOnDotDotDot() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "x";
        final String xpathProperty = "gmd:hierarchyLevel/.../@codeListValue";
        assertFalse(new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true));
    }

    @Test
    public void testNoUpdateOnError() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String newValue = "x";
        final String xpathProperty = "gmd:hierarchyLevel/gmd:MD_ScopeCode/../gmd:fileIdentifier";
        try {
            new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
                    new AddElemValue(newValue), true);
            fail("expected error");
        } catch (AssertionError e) {
            // ignore.
        }
        assertEquals(0, metadataElement.getContentSize());
    }

    @Test(expected = AssertionError.class)
    public void testAddAttribute_ValueIsNode2() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "<service>xml is not allowed here</service>";
        final String xpathProperty = "@codeListValue";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue),
                true);
    }


    @Test
    public void multiUpdate() throws JDOMException {
        SchemaManager manager = _schemaManager;
        MetadataSchema schema = manager.getSchema("iso19139");
        final Element metadataElement = new Element("MD_Metadata", GMD);
        HashMap<String, AddElemValue> updates = new HashMap<String, AddElemValue>();
        final String text = "text";
        final String att = "att";
        final String charStringXpath = "gmd:fileIdentifier/gco:CharacterString";
        final String attXPath = "gmd:fileIdentifier/@att";
        updates.put(charStringXpath, new AddElemValue(text));
        updates.put(attXPath, new AddElemValue(att));

        final int numUpdates = new EditLib(_schemaManager).addElementOrFragmentFromXpaths(metadataElement, updates, schema, true);

        assertEquals(2, numUpdates);

        assertEqualsText(text, metadataElement, charStringXpath, GMD, GCO);
        assertEquals(att, Xml.selectString(metadataElement, attXPath, Arrays.asList(GMD, GCO)));
    }

    @Test
    public void testStackOverflowFindLongestMatch() throws Exception {
        final Element element = Xml.loadFile(EditLibTest.class.getResource("editlib-stackoverflow-findlongestmatch.xml"));
        List<String> xpathParts = Lists.newArrayList("gmd:distributionInfo", "gmd:MD_Distribution", "gmd:transferOptions",
                "gmd:MD_DigitalTransferOptions", "gmd:onLine", "gmd:CI_OnlineResource", "gmd:linkage");
        MetadataSchema schema = _schemaManager.getSchema("iso19139");
        final EditLib editLib = new EditLib(_schemaManager);
        final Pair<Element, String> longestMatch = editLib.findLongestMatch(element, schema, xpathParts);
        assertEquals("gmd:MD_Distribution", longestMatch.one().getQualifiedName());
        assertEquals("gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage", longestMatch.two());
    }
}
