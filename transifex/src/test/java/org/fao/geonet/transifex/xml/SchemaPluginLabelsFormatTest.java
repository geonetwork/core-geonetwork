package org.fao.geonet.transifex.xml;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.sf.json.JSONObject;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 6/18/2015.
 */
public class SchemaPluginLabelsFormatTest {

    @Test
    public void testToTransifex() throws Exception {
        SchemaPluginLabelsFormat format = new SchemaPluginLabelsFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.categories = Sets.newHashSet("cat1");
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/labels.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        assertEquals(4, files.size());

        Map<String, TransifexReadyFile> filesMap = toMap(files);

        assertTrue(files.get(0).categories.contains("cat1"));
        assertTranslations(filesMap, "id-label",
                "labels/element[name=\"gmd:MD_ScopeCode\"]", "Scope code",
                "labels/element[name=\"code\",id=\"207.0\",context=\"gmd:MD_Identifier\"]", "Code",
                "labels/element[name=\"code\"]", "System code");

        assertTranslations(filesMap, "id-description",
                "labels/element[name=\"gmd:MD_ScopeCode\"]", "Class of information to which the referencing entity applies",
                "labels/element[name=\"code\",id=\"207.0\",context=\"gmd:MD_Identifier\"]", "Alphanumeric value identifying an instance in the namespace",
                "labels/element[name=\"code\"]", "Code. i.e. EPSG code");

        assertTranslations(filesMap, "id-help",
                "labels/element[name=\"code\",id=\"207.0\",context=\"gmd:MD_Identifier\"]", "alphanumeric value identifying an instance in the namespace");

        assertTranslations(filesMap, "id-condition",
                "labels/element[name=\"code\"]", "Use obligation/condition from referencing object");


    }

    private void assertTranslations(Map<String, TransifexReadyFile> filesMap, String fileId, String... properties) {
        TransifexReadyFile labels = filesMap.get(fileId);
        assertNotNull(labels);
        JSONObject labelsJSON = JSONObject.fromObject(labels.data);
        assertEquals(properties.length / 2, labelsJSON.size());
        for (int i = 0; i < properties.length / 2; i+=2) {
            assertEquals(properties[i + 1], labelsJSON.getString(properties[i]));
        }
    }


    private Map<String, TransifexReadyFile> toMap(List<TransifexReadyFile> files) {
        Map<String, TransifexReadyFile> map = Maps.newHashMap();
        for (TransifexReadyFile file : files) {
            map.put(file.resourceId, file);
        }
        return map;
    }

    @Test
    public void testToGeonetwork() throws Exception {
        SchemaPluginLabelsFormat format = new SchemaPluginLabelsFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.categories = Sets.newHashSet("cat1");
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/labels.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        String recreated = format.toGeonetwork(files);

        Element loadedXml = Xml.loadString(recreated, false);
        assertEquals(3, Xml.selectNodes(loadedXml, "element/label").size());
        assertEquals(3, Xml.selectNodes(loadedXml, "element/description").size());
        assertEquals(1, Xml.selectNodes(loadedXml, "element/help").size());
        assertEquals(1, Xml.selectNodes(loadedXml, "element/_condition").size());

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

    }
}