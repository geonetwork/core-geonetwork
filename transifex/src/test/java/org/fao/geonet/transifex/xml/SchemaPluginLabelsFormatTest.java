package org.fao.geonet.transifex.xml;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.fao.geonet.transifex.TransifexReadyFile;
import org.fao.geonet.transifex.TranslationFileConfig;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 6/18/2015.
 */
public class SchemaPluginLabelsFormatTest extends AbstractXmlFormatTest {

    @Test
    public void testToTransifex() throws Exception {
        SchemaPluginLabelsFormat format = new SchemaPluginLabelsFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.fileName = "labels.xml";
        config.categories = Sets.newHashSet("cat1");
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/labels.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        assertEquals(5, files.size());

        Map<String, TransifexReadyFile> filesMap = toMap(files);

        assertTrue(files.get(0).categories.contains("cat1"));
        assertTranslations(filesMap, "id-label",
                "labels/element[name='gmd:MD_ScopeCode']", "Scope code",
                "labels/element[name='code' and id='207.0' and context='gmd:MD_Identifier']", "Code",
                "labels/element[name='code']", "System code");

        assertTranslations(filesMap, "id-description",
                "labels/element[name='gmd:MD_ScopeCode']", "Class of information to which the referencing entity applies",
                "labels/element[name='code' and id='207.0' and context='gmd:MD_Identifier']", "Alphanumeric value identifying an instance in the namespace",
                "labels/element[name='code']", "Code. i.e. EPSG code.");

        assertTranslations(filesMap, "id-help",
                "labels/element[name='code' and id='207.0' and context='gmd:MD_Identifier']", "alphanumeric value identifying an instance in the namespace");

        assertTranslations(filesMap, "id-condition",
                "labels/element[name='code']", "Use obligation/condition from referencing object");
        assertTranslations(filesMap, "id-helper",
                "labels/element[name='code']/helper/option[value='CH']", "Switzerland",
                "labels/element[name='code']/helper/option[value='DE']", "Germany");


    }


    @Test
    public void testToGeonetwork() throws Exception {
        SchemaPluginLabelsFormat format = new SchemaPluginLabelsFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.categories = Sets.newHashSet("cat1");
        config.fileName = "labels.xml";
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/labels.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        String recreated = format.toGeonetwork(files);

        Element loadedXml = Xml.loadString(recreated, false);
        assertEquals(3, Xml.selectNodes(loadedXml, "element/label").size());
        assertEquals(3, Xml.selectNodes(loadedXml, "element/description").size());
        assertEquals(1, Xml.selectNodes(loadedXml, "element/help").size());
        assertEquals(1, Xml.selectNodes(loadedXml, "element/_condition").size());
        assertEquals(2, Xml.selectNodes(loadedXml, "element/helper/option").size());

        assertEquals(2, Xml.selectNodes(loadedXml, "element[@name='code']").size());

        Element codeWithContext = Xml.selectElement(loadedXml, "element[@name='code' and @id='207.0' and @context='gmd:MD_Identifier']");
        Element codeNoContext = Xml.selectElement(loadedXml, "element[@name='code' and not(@context)]");
        Element scopeCode = Xml.selectElement(loadedXml, "element[@name='gmd:MD_ScopeCode']");

        assertEquals(1, Xml.selectNodes(loadedXml, "element[@name='code' and @id='207.0' and @context='gmd:MD_Identifier']").size());
        assertNull(codeWithContext.getChild("_condition"));
        assertEquals("Code", codeWithContext.getChildText("label"));
        assertEquals("Alphanumeric value identifying an instance in the namespace", codeWithContext.getChildText("description"));
        assertEquals("alphanumeric value identifying an instance in the namespace", codeWithContext.getChildText("help"));

        assertEquals(1, Xml.selectNodes(loadedXml, "element[@name='code' and not(@context)]").size());
        assertNull(codeNoContext.getChild("help"));
        assertEquals("System code", codeNoContext.getChildText("label"));
        assertEquals("Code. i.e. EPSG code.", codeNoContext.getChildText("description"));
        assertEquals("Use obligation/condition from referencing object", codeNoContext.getChildText("_condition"));

        assertEquals(1, Xml.selectNodes(loadedXml, "element[@name='gmd:MD_ScopeCode']").size());
        assertNull(scopeCode.getChild("_condition"));
        assertNull(scopeCode.getChild("help"));
        assertEquals("Scope code", scopeCode.getChildText("label"));
        assertEquals("Class of information to which the referencing entity applies", scopeCode.getChildText("description"));
        assertEquals(2, Xml.selectNodes(codeNoContext, "helper/option").size());
        assertEquals("Switzerland", Xml.selectString(codeNoContext, "helper/option[@value='CH']"));
        assertEquals("Germany", Xml.selectString(codeNoContext, "helper/option[@value='DE']"));

    }
}