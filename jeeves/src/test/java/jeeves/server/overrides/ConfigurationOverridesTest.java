package jeeves.server.overrides;


import jeeves.config.springutil.JeevesApplicationContext;
import org.apache.log4j.Level;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigurationOverridesTest {
	private static final ClassLoader classLoader;
    private static final String appPath;
    private static final String falseAppPath;
    private static final ConfigurationOverrides.ResourceLoader loader;
    
    static {
        try {
            classLoader = ConfigurationOverridesTest.class.getClassLoader();
            String base = URLDecoder.decode(classLoader.getResource("test-config.xml").getFile(), Constants.ENCODING);
            appPath = new File(new File(base).getParentFile(), "correct-webapp").getAbsolutePath();
            falseAppPath = new File(new File(base).getParentFile(), "false-webapp").getAbsolutePath();
            loader = new ConfigurationOverrides.ServletResourceLoader(null, appPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test //@Ignore
    public void updateLoggingConfig() throws JDOMException, IOException {
        final Element overrides = Xml.loadFile(classLoader.getResource("correct-webapp/WEB-INF/overrides-config.xml"));

        ConfigurationOverrides.DEFAULT.doUpdateLogging(overrides, loader);
        assertEquals(Level.DEBUG, org.apache.log4j.Logger.getRootLogger().getLevel());
    }
    @Test //@Ignore
    public void imports() throws JDOMException, IOException {
        Element config = loader.loadXmlResource("/WEB-INF/overrides-config.xml");
        assertEquals(6, Xml.selectElement(config,"properties").getChildren().size());
        assertEquals(10, Xml.selectElement(config,"file[@name = 'config.xml']").getChildren().size());
        assertEquals(1, Xml.selectNodes(config,"file[@name = 'config3.xml']").size());
        assertEquals("fre", Xml.selectElement(config,"properties/*[1]").getName());
        assertEquals("removeXML", Xml.selectElement(config,"file[1]/*[1]").getName());
        assertEquals("overridden", Xml.selectString(config,"properties/aparam"));
    }
    @Test
    public void updateConfig() throws JDOMException, IOException {
        Element config = Xml.loadFile(classLoader.getResource("test-config.xml"));
        Element config2 = (Element) Xml.loadFile(classLoader.getResource("test-config.xml")).clone();

        ConfigurationOverrides.DEFAULT.updateWithOverrides("config.xml", null, appPath, config);
        ConfigurationOverrides.DEFAULT.updateWithOverrides("config2.xml", null, appPath, config2);

        assertLang("fre", config);
        assertLang("ger", config2);

        assertEquals("xml/europeanCountries.xml", Xml.selectString(config, "default/gui/xml[@name = 'countries']/@file"));
        assertEquals("xml/other.xml", Xml.selectString(config2, "default/gui/xml[@name = 'countries']/@file"));

        assertTrue(Xml.selectNodes(config, "default/gui/@removeAtt").isEmpty());
        assertEquals(1, Xml.selectNodes(config, "default/gui/@newAtt").size());
        assertEquals("newValue", Xml.selectString(config, "default/gui/@newAtt"));

        assertEquals(1, Xml.selectElement(config, "resources").getChildren().size());
        assertEquals(1, Xml.selectNodes(config, "resources/resource/config/url").size());
        assertEquals("jdbc:oracle:thin:@localhost:1521:fs", Xml.selectElement(config, "resources/resource/config/url").getTextTrim());

        assertTrue(Xml.selectNodes(config, "*//toRemove").isEmpty());
        assertTrue(Xml.selectNodes(config, "*//gui/xml[@name = countries2]").isEmpty());
        assertEquals(1, Xml.selectNodes(config, "newNode").size());
        assertEquals(1, Xml.selectNodes(config, "default/gui").size());

        assertEquals(1, Xml.selectNodes(config, "default/gui/text()").size());
        assertEquals("ExtraText", Xml.selectString(config, "default/gui/text()"));
    }

    private void assertLang(String expected, Element config) throws JDOMException {
        List<?> lang = Xml.selectNodes(config,"*//language");
        assertEquals(1,lang.size());
        assertEquals(Xml.getString(config), expected, ((Element)lang.get(0)).getTextTrim());
    }
    @Test //@Ignore
    public void loadFile() throws JDOMException, IOException {
    	URL resourceAsStream = classLoader.getResource("test-sql.sql");
    	BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream.openStream(), Constants.ENCODING));
    	try {
    	    // note first , is intentional to verify that it will be ignored
			List<String> lines = new ConfigurationOverrides("/WEB-INF/overrides-config.xml,/WEB-INF/overrides-config-overlay.xml").loadTextFileAndUpdate("test-sql.sql", null, appPath, reader);
			
			assertEquals("CREATE TABLE NewRelations", lines.get(0).trim());
			assertEquals("(", lines.get(1).trim());
			assertEquals("primary key(id,overridden)", lines.get(2).trim());
			assertEquals(");", lines.get(3).trim());
			assertEquals("INSERT INTO Settings VALUES (21,20,'host','localhost');", lines.get(4).trim());
			assertEquals("INSERT INTO Settings VALUES (22,20,'port','8080');", lines.get(5).trim());
    	}finally {
    		reader.close();
    	}
    }
    @Test //@Ignore
    public void updateSpringConfiguration() throws JDOMException, IOException {
        final ConfigurationOverrides configurationOverrides = new ConfigurationOverrides("/WEB-INF/test-spring-config-overrides.xml");
        JeevesApplicationContext applicationContext = new JeevesApplicationContext(configurationOverrides, null, "classpath:test-spring-config.xml"){

            @Override
            protected String getAppPath() {
                return appPath;
            }
        };
        updateAndPerformSpringAssertions(applicationContext);
        
        // make sure refresh works multiple times
        updateAndPerformSpringAssertions(applicationContext);
        
        // make sure refresh works multiple times
        updateAndPerformSpringAssertions(applicationContext);
    }
    private void updateAndPerformSpringAssertions(JeevesApplicationContext applicationContext) {
        applicationContext.refresh();

        ExampleBean testBeanFull = applicationContext.getBean("testBeanFull", ExampleBean.class);
        ExampleBean testBean = applicationContext.getBean("testBean", ExampleBean.class);
        ExampleBean testBean2 = applicationContext.getBean("testBean2", ExampleBean.class);
        ExampleBean testBean3 = applicationContext.getBean("testBean3", ExampleBean.class);
        
        assertNotNull(testBeanFull);
        assertNotNull(testBean);
        assertNotNull(testBean2);
        assertNotNull(testBean3);

        assertEquals("updatedBasicProp", testBeanFull.getBasicProp());
        assertEquals("updatedBasicProp2", testBeanFull.getBasicProp2());
        assertEquals(2, testBeanFull.getCollectionProp().size());
        assertTrue(testBeanFull.getCollectionProp().contains("addedProperty"));
        assertTrue(testBeanFull.getCollectionProp().contains("value1"));
        assertEquals(2, testBeanFull.getCollectionRef().size());
        assertTrue(testBeanFull.getCollectionRef().contains(testBean));
        assertTrue(testBeanFull.getCollectionRef().contains(testBean2));

        assertEquals("overriddenProp", testBean.getBasicProp());
        assertEquals(testBean2, testBean.getSimpleRef());
        assertTrue("testbean should have a testbean added to one of its collections", testBean.getCollectionRef().contains(testBean3));
        assertEquals("astring", testBean.getBasicProp2());
        assertTrue("testBeans doesn't contain 'newString' in its collection of strings", testBean.getCollectionProp().contains("newString"));
        
        FilterSecurityInterceptor filterSecurityInterceptor = applicationContext.getBean("filterSecurityInterceptor", FilterSecurityInterceptor.class);
        Collection<ConfigAttribute> attributes = filterSecurityInterceptor.getSecurityMetadataSource().getAllConfigAttributes();
        assertInterceptUrl(attributes, "hasRole('Administrator')");
        assertInterceptUrl(attributes, "hasRole('RegisteredUser')");
        assertNotInterceptUrl(attributes, "hasRole('REMOVE')");
        assertNotInterceptUrl(attributes, "hasRole('SET')");
    }
    private void assertInterceptUrl(Collection<ConfigAttribute> attributes, String expectedExp) {
        assertInterceptUrl(attributes, expectedExp, true);
    }
    private void assertNotInterceptUrl(Collection<ConfigAttribute> attributes, String expectedExp) {
        assertInterceptUrl(attributes, expectedExp, false);
    }
    private void assertInterceptUrl(Collection<ConfigAttribute> attributes, String expectedExp, boolean assertTrue) {
        boolean found = false;
        for (ConfigAttribute configAttribute : attributes) {
            if(configAttribute.toString().equals(expectedExp)) {
                found = true;
            }
        }

        if(assertTrue) {
            assertTrue(attributes+" does not contain "+expectedExp, found);
        } else {
            assertFalse(attributes+" contains "+expectedExp, found);
        }
    }

    @Test //@Ignore
    public void noUpdateConfig() throws JDOMException, IOException {
        Element config = Xml.loadFile(classLoader.getResource("test-config.xml"));
        Element unchanged = (Element) config.clone();
        ConfigurationOverrides.DEFAULT.updateWithOverrides("config.xml", null, falseAppPath, config);

        assertLang("eng",config);

        assertEquals(Xml.selectString(unchanged,"default/gui/xml[@name = 'countries']/@file"), Xml.selectString(config,"default/gui/xml[@name = 'countries']/@file"));
    }
    
    // TODO no property
    // no overrides
    // invalid appPath
    
}
