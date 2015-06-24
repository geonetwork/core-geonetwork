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
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 6/19/2015.
 */
public class SimpleElementFormatTest extends AbstractXmlFormatTest {

    @Test
    public void testToTransifex() throws Exception {
        SimpleElementFormat format = new SimpleElementFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.fileName = "nested-strings.xml";
        config.categories = Sets.newHashSet("cat1");
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/nested-strings.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        assertEquals(1, files.size());
        Map<String, TransifexReadyFile> filesMap = toMap(files);

        assertTrue(files.get(0).categories.contains("cat1"));
        assertTranslations(filesMap, "id-string",
                "strings/schematron.title", "Geonetwork Rules",
                "strings/M500", "[Language] - Metadata language is not",
                "strings/alert.M500", "\n"
                                      + "        <!-- A Comment -->\n"
                                      + "        <div>divData</div>\n"
                                      + "    ");
    }

    @Test
    public void testToGeonetwork() throws Exception {
        SimpleElementFormat format = new SimpleElementFormat();
        TranslationFileConfig config = new TranslationFileConfig();
        config.id = "id";
        config.name = "name";
        config.categories = Sets.newHashSet("cat1");
        config.fileName = "nested-strings.xml";
        format.configure(config, Maps.<String, String>newHashMap());
        Path path = Paths.get(SchemaPluginLabelsFormatTest.class.getResource("eng/nested-strings.xml").toURI()).getParent().getParent();
        List<TransifexReadyFile> files = format.toTransifex(path.toString());

        String recreated = format.toGeonetwork(files);

        Element loadedXml = Xml.loadString(recreated, false);
        assertEquals(3, loadedXml.getChildren().size());

        assertEquals("Geonetwork Rules", Xml.selectString(loadedXml, "schematron.title"));
        assertEquals("[Language] - Metadata language is not", Xml.selectString(loadedXml, "M500"));
        assertEquals("divData", Xml.selectString(loadedXml, "alert.M500/div"));
    }
}