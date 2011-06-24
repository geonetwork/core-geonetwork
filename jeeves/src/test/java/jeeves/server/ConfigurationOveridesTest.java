package jeeves.server;


import jeeves.utils.Xml;
import org.apache.log4j.Level;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationOveridesTest {
    final ClassLoader classLoader = getClass().getClassLoader();
    final ConfigurationOverrides.ResourceLoader loader = new ClasspathResourceLoader();


    @Test
    public void updateLoggingConfig() throws JDOMException, IOException {
        final Element overrides = Xml.loadFile(classLoader.getResource("config-overrides.xml"));

        ConfigurationOverrides.doUpdateLogging(overrides, loader);
        assertEquals(Level.DEBUG, org.apache.log4j.Logger.getRootLogger().getLevel());
    }
    @Test
    public void imports() throws JDOMException, IOException {
        Element config = loader.loadXmlResource("config-overrides.xml");
        assertEquals(6, Xml.selectElement(config,"properties").getChildren().size());
        assertEquals(10, Xml.selectElement(config,"file[@name = 'config.xml']").getChildren().size());
        assertEquals(1, Xml.selectNodes(config,"file[@name = 'config3.xml']").size());
        assertEquals("fr", Xml.selectElement(config,"properties/*[1]").getName());
        assertEquals("removeXML", Xml.selectElement(config,"file[1]/*[1]").getName());
        assertEquals("overridden", Xml.selectString(config,"properties/aparam"));
    }
    @Test
    public void updateConfig() throws JDOMException, IOException {
        Element config = Xml.loadFile(classLoader.getResource("test-config.xml"));
        Element config2 = (Element) Xml.loadFile(classLoader.getResource("test-config.xml")).clone();
        ConfigurationOverrides.updateConfig(loader, "config-overrides.xml","config.xml", config);
        ConfigurationOverrides.updateConfig(loader, "config-overrides.xml","config2.xml", config2);

        assertLang("fr",config);
        assertLang("de", config2);

        assertEquals("xml/europeanCountries.xml", Xml.selectString(config,"default/gui/xml[@name = 'countries']/@file"));
        assertEquals("xml/other.xml", Xml.selectString(config2,"default/gui/xml[@name = 'countries']/@file"));

        assertTrue(Xml.selectNodes(config,"default/gui/@removeAtt").isEmpty());
        assertEquals(1,Xml.selectNodes(config,"default/gui/@newAtt").size());
        assertEquals("newValue",Xml.selectString(config,"default/gui/@newAtt"));

        assertEquals(1,Xml.selectElement(config,"resources").getChildren().size());
        assertEquals(1,Xml.selectNodes(config,"resources/resource/config/url").size());
        assertEquals("jdbc:oracle:thin:@localhost:1521:fs",Xml.selectElement(config,"resources/resource/config/url").getTextTrim());

        assertTrue(Xml.selectNodes(config,"*//toRemove").isEmpty());
        assertTrue(Xml.selectNodes(config,"*//gui/xml[@name = countries2]").isEmpty());
        assertEquals(1, Xml.selectNodes(config,"newNode").size());
        assertEquals(1, Xml.selectNodes(config,"default/gui").size());

        assertEquals(1, Xml.selectNodes(config,"default/gui/text()").size());
        assertEquals("ExtraText", Xml.selectString(config,"default/gui/text()"));
    }

    private void assertLang(String expected, Element config) throws JDOMException {
        List<?> lang = Xml.selectNodes(config,"*//language");
        assertEquals(1,lang.size());
        assertEquals(expected, ((Element)lang.get(0)).getTextTrim());
    }

    class ClasspathResourceLoader extends ConfigurationOverrides.ResourceLoader {

        @Override
        protected InputStream loadInputStream(String resource) throws JDOMException, IOException {
            return classLoader.getResourceAsStream(resource);
        }
    }
}
