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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 6/19/2015.
 */
public class SchemaPluginCodelistFormatTest extends AbstractXmlFormatTest {

    @Test
    public void testToTransifex() throws Exception {
        SchemaPluginCodelistFormat format = new SchemaPluginCodelistFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.fileName = "codelists.xml";
        config.categories = Sets.newHashSet("cat1");
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/codelists.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        assertEquals(1, files.size());
        Map<String, TransifexReadyFile> filesMap = toMap(files);

        assertTrue(files.get(0).categories.contains("cat1"));
        assertTranslations(filesMap, "id",
                "gmd:CI_DateTypeCode/creation/label", "Creation",
                "gmd:CI_DateTypeCode/publication/label", "Publication",
                "gmd:CI_DateTypeCode/revision[hideInEditMode=true]/label", "Revision",
                "gmd:CI_DateTypeCode/creation/description", "Date identifies when the resource was brought into existence",
                "gmd:CI_DateTypeCode/publication/description", "Date identifies when the resource was issued");
    }

    @Test
    public void testToGeonetwork() throws Exception {
        SchemaPluginCodelistFormat format = new SchemaPluginCodelistFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.categories = Sets.newHashSet("cat1");
        config.fileName = "codelists.xml";
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/codelists.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        String recreated = format.toGeonetwork(files);

        Element loadedXml = Xml.loadString(recreated, false);
        assertEquals(3, Xml.selectNodes(loadedXml, "*//entry/code").size());
        assertEquals(3, Xml.selectNodes(loadedXml, "*//entry/label").size());
        assertEquals(2, Xml.selectNodes(loadedXml, "*//entry/description").size());

        Element creation = Xml.selectElement(loadedXml, "*//entry[code = 'creation']");
        Element publication = Xml.selectElement(loadedXml, "*//entry[code = 'publication']");
        Element revision = Xml.selectElement(loadedXml, "*//entry[code = 'revision']");

        assertNotNull(creation);
        assertNotNull(publication);
        assertNotNull(revision);


        assertEquals("Creation", creation.getChildText("label"));
        assertEquals("Date identifies when the resource was brought into existence", creation.getChildText("description"));
        assertNull(publication.getAttribute(SchemaPluginCodelistFormat.HIDE_IN_EDIT_MODE));

        assertEquals("Publication", publication.getChildText("label"));
        assertEquals("Date identifies when the resource was issued", publication.getChildText("description"));
        assertNull(publication.getAttribute(SchemaPluginCodelistFormat.HIDE_IN_EDIT_MODE));

        assertEquals("Revision", revision.getChildText("label"));
        assertEquals(0, revision.getChildren("description").size());
        assertEquals("true", revision.getAttributeValue(SchemaPluginCodelistFormat.HIDE_IN_EDIT_MODE));

    }
}