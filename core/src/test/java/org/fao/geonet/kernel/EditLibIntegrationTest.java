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

package org.fao.geonet.kernel;

import com.google.common.collect.Lists;

import junit.framework.Assert;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.fao.geonet.Assert.assertFalse;
import static org.fao.geonet.Assert.assertTrue;
import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;

/**
 * Test EditLib methods.
 *
 * Created by Jesse on 12/6/13.
 */
public class EditLibIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SchemaManager _schemaManager;

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

        Assert.assertEquals(newValue, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
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


        newValue = "newValue2";
        final String updateXPath2 = "(descendant-or-self::gmd:RS_Identifier)[2]/gmd:code/gco:CharacterString";
        updateOccurred = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, updateXPath2,
            new AddElemValue(newValue), true);

        assertTrue(updateOccurred);
        assertEqualsText(newValue, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);

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
    public void testReplaceAllMathFromXpath_SpecialReplaceTag() throws Exception {

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

        Element newRefSystemsCode = new Element(EditLib.SpecialUpdateTags.REPLACE).setText(newValue1);
        final String xpathToUpdate = "*//gmd:code/gco:CharacterString";

        final String refSysElemName = "gmd:referenceSystemInfo";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(
            metadataElement, schema, xpathToUpdate,
            new AddElemValue(newRefSystemsCode), true);

        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString";
        assertEqualsText(newValue1, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(newValue1, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);
        assertEqualsText(newValue1, metadataElement, refSysElemName + "[3]" + xpath, GMD, GCO);
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }


    @Test
    public void testReplaceFragmentFromXpath_SpecialDeleteTag() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
            createReferenceSystemInfo(code1),
            createReferenceSystemInfo(code2),
            createReferenceSystemInfo(code3)
        ));
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());


        Element deleteNotExistingElement = new Element(EditLib.SpecialUpdateTags.DELETE);
        final String xpathWithNoMatch = "gmd:referenceSystemInfo[*/referenceSystemIdentifier/*/gmd:code/*/text() = 'XYZ']";
        new EditLib(_schemaManager)
            .addElementOrFragmentFromXpath(metadataElement, schema,
                xpathWithNoMatch, new AddElemValue(deleteNotExistingElement), true);

        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());

        new EditLib(_schemaManager)
            .addElementOrFragmentFromXpath(metadataElement, schema,
                xpathWithNoMatch, new AddElemValue("<gn_delete/>"), true);

        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());

        new EditLib(_schemaManager)
            .addElementOrFragmentFromXpath(metadataElement, schema,
                xpathWithNoMatch, new AddElemValue("<gn_delete></gn_delete>"), true);

        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());


        Element newRefSystems = new Element(EditLib.SpecialUpdateTags.DELETE);
        final String refSysElemName = "gmd:referenceSystemInfo";
        // Delete first node
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema,
            refSysElemName + "[1]", new AddElemValue(newRefSystems), true);

        final String xpath = "/gmd:ReferenceSystem/gmd:referenceSystemIdentifier/" +
            "gmd:RS_Identifier/gmd:code/gco:CharacterString";

        assertEquals(2, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
        assertEqualsText(code2, metadataElement, refSysElemName + "[1]" + xpath, GMD, GCO);
        assertEqualsText(code3, metadataElement, refSysElemName + "[2]" + xpath, GMD, GCO);


        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema,
            refSysElemName, new AddElemValue(newRefSystems), true);

        assertEquals(0, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
    }


    @Test
    public void testReplaceAll() throws Exception {
        MetadataSchema schema = _schemaManager.getSchema("iso19139");
        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
            createReferenceSystemInfo(code1),
            createReferenceSystemInfo(code2),
            createReferenceSystemInfo(code3)
        ));
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());

        final String refSysElemName = "gmd:referenceSystemInfo";
        String updateConfig = "<gn_replace_all><gmd:referenceSystemInfo xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"><gmd:code>NEW</gmd:code></gmd:referenceSystemInfo></gn_replace_all>";

        LinkedHashMap<String, AddElemValue> updates = new LinkedHashMap<String, AddElemValue>();
        final String attXPath = "gmd:referenceSystemInfo";
        updates.put(attXPath, new AddElemValue(updateConfig));
        new EditLib(_schemaManager).addElementOrFragmentFromXpaths(metadataElement, updates, schema, true);

        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
        assertEqualsText("NEW", metadataElement, refSysElemName + "[1]/gmd:code", GMD, GCO);
    }

    @Test
    public void testReplaceFragmentFromXpath_SpecialDeleteAllTag() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        final Element metadataElement = new Element("MD_Metadata", GMD).addContent(Arrays.asList(
            createReferenceSystemInfo(code1),
            createReferenceSystemInfo(code2),
            createReferenceSystemInfo(code3)
        ));
        assertEquals(3, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());

        Element newRefSystems = new Element(EditLib.SpecialUpdateTags.DELETE);
        final String refSysElemName = "gmd:referenceSystemInfo";
        // Delete all nodes of this type
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema,
            refSysElemName, new AddElemValue(newRefSystems), true);

        assertEquals(0, Xml.selectNodes(metadataElement, "gmd:referenceSystemInfo", Arrays.asList(GMD, GCO)).size());
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
    public void testAddFragmentFromXpath_ForNonExistingElementShouldCreateThem() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);

        String newValue =
            "<gmd:date xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">" +
                "    <gmd:CI_Date>" +
                "      <gmd:date>" +
                "        <gco:Date>2014-01-21</gco:Date>" +
                "      </gmd:date>" +
                "      <gmd:dateType>" +
                "        <gmd:CI_DateTypeCode codeListValue=\"revision\"></gmd:CI_DateTypeCode>" +
                "      </gmd:dateType>" +
                "    </gmd:CI_Date>" +
                "  </gmd:date>";
        // Test with no condition
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement, schema,
            "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date", new AddElemValue(newValue), true);
        assertEqualsText("2014-01-21", metadataElement,
            "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation" +
                "/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision']/gmd:CI_Date/gmd:date/gco:Date", GMD, GCO);

        //Test with condition
        final Element metadataElement2 = new Element("MD_Metadata", GMD);
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement2, schema,
            "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation" +
                "/gmd:CI_Citation/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision']", new AddElemValue(newValue), true);
        assertEqualsText("2014-01-21", metadataElement2,
            "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation" +
                "/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision']/gmd:CI_Date/gmd:date/gco:Date", GMD, GCO);

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

        Assert.assertEquals(newValue, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel/gmd:MD_ScopeCode", Arrays.asList(GMD, GCO)).size());
        assertEquals(1, Xml.selectNodes(metadataElement, "gmd:hierarchyLevel", Arrays.asList(GMD, GCO)).size());

        String newValue2 = "dataset";
        new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, new AddElemValue(newValue2),
            true);

        Assert.assertEquals(newValue2, Xml.selectString(metadataElement, xpathProperty, Arrays.asList(GMD, GCO)));
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
        assertFalse(new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
            new AddElemValue(newValue),
            true));
    }

    @Test
    public void testErrorOnDotDotDot() throws Exception {
        SchemaManager manager = _schemaManager;

        MetadataSchema schema = manager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "x";
        final String xpathProperty = "gmd:hierarchyLevel/.../@codeListValue";
        assertFalse(new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
            new AddElemValue(newValue),
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
            Assert.fail("expected error");
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
    public void testMultiUpdate() throws JDOMException, IOException {
        SchemaManager manager = _schemaManager;
        MetadataSchema schema = manager.getSchema("iso19139");
        final Element metadataElement = new Element("MD_Metadata", GMD);
        LinkedHashMap<String, AddElemValue> updates = new LinkedHashMap<String, AddElemValue>();
        final String text = "text";
        final String att = "att";
        final String charStringXpath = "gmd:fileIdentifier/gco:CharacterString";
        final String attXPath = "gmd:fileIdentifier/@att";
        updates.put(attXPath, new AddElemValue(att));
        updates.put(charStringXpath, new AddElemValue(text));

        final int numUpdates = new EditLib(_schemaManager).addElementOrFragmentFromXpaths(metadataElement, updates, schema, true);

        assertEquals(2, numUpdates);

        assertEqualsText(text, metadataElement, charStringXpath, GMD, GCO);
        Assert.assertEquals(att, Xml.selectString(metadataElement, attXPath, Arrays.asList(GMD, GCO)));
    }

    /*
     * BUG: This test exposes a bug in the EditLib. If the xpath points to an existing element,
     * adding an attribute to the existing element does not work.
     */
    @Test
    public void testAddAttributeExistingElement() throws JDOMException, IOException {
        SchemaManager manager = _schemaManager;
        MetadataSchema schema = manager.getSchema("iso19139");
        final Element metadataElement = new Element("MD_Metadata", GMD);
        LinkedHashMap<String, AddElemValue> updates = new LinkedHashMap<String, AddElemValue>();
        final String text = "text";
        final String att = "att";
        final String charStringXpath = "gmd:fileIdentifier/gco:CharacterString";
        final String attXPath = "gmd:fileIdentifier/@att";
        updates.put(attXPath, new AddElemValue(att));
        updates.put(charStringXpath, new AddElemValue(text));

        EditLib el = new EditLib(_schemaManager);
        boolean a1 = el.addElementOrFragmentFromXpath(metadataElement, schema, charStringXpath, new AddElemValue(text), true);
        boolean a2 = el.addElementOrFragmentFromXpath(metadataElement, schema, attXPath, new AddElemValue(att), true);

        assertEquals(true, a1 && a2);

        assertEqualsText(text, metadataElement, charStringXpath, GMD, GCO);
        assertEquals(att, Xml.selectString(metadataElement, attXPath, Arrays.asList(GMD, GCO)));
    }


    @Test
    public void testAddElementFromXpath_Extent() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "newValueFromRootXpath";
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement";
        final String xpathProperty = xpath;
        boolean updated = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
            new AddElemValue(newValue),
            true);

        assertTrue(updated);
        assertEqualsText(newValue, metadataElement, xpath, GMD, GCO);
    }

    @Test
    public void testAddElementFromXpath_ExtentFromRoot() throws Exception {

        MetadataSchema schema = _schemaManager.getSchema("iso19139");

        final Element metadataElement = new Element("MD_Metadata", GMD);
        String newValue = "newValueFromRootXpath";
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement";
        final String xpathProperty = "/gmd:MD_Metadata/" + xpath;
        boolean updated = new EditLib(_schemaManager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty,
            new AddElemValue(newValue),
            true);

        assertTrue(updated);
        assertEqualsText(newValue, metadataElement, xpath, GMD, GCO);
    }

    @Test
    public void testStackOverflowFindLongestMatch() throws Exception {
        final Element element = Xml.loadFile(EditLibIntegrationTest.class.getResource("editlib-stackoverflow-findlongestmatch.xml"));
        List<String> xpathParts = Lists.newArrayList("gmd:distributionInfo", "gmd:MD_Distribution", "gmd:transferOptions",
            "gmd:MD_DigitalTransferOptions", "gmd:onLine", "gmd:CI_OnlineResource", "gmd:linkage");
        MetadataSchema schema = _schemaManager.getSchema("iso19139");
        final EditLib editLib = new EditLib(_schemaManager);
        final Pair<Element, String> longestMatch = editLib.findLongestMatch(element, schema, xpathParts);
        Assert.assertEquals("gmd:MD_Distribution", longestMatch.one().getQualifiedName());
        Assert.assertEquals("gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage", longestMatch.two());
    }
}
