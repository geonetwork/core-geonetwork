package org.fao.geonet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * A helper class for testing services.  This super-class loads in the spring beans for Spring-data repositories and mocks for
 * some of the system that is required by services.
 *
 * User: Jesse
 * Date: 10/12/13
 * Time: 8:31 PM
 */
public abstract class AbstractCoreTest extends AbstractSpringDataTest {
    @Autowired
    ConfigurableApplicationContext _applicationContext;
    @PersistenceContext
    EntityManager _entityManager;

    /**
     * Create a Service context without a user session but otherwise ready to use.
     */
    protected ServiceContext createServiceContext() throws Exception {
        final HashMap<String, Object> contexts = new HashMap<String, Object>();
        final Constructor<GeonetContext> constructor = GeonetContext.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        GeonetContext gc = constructor.newInstance();
        contexts.put(Geonet.CONTEXT_NAME, gc);
        return new ServiceContext("mockService", _applicationContext, contexts, _entityManager);
    }

    /**
     * Check if an element exists and if it has the expected test.
     *
     * @param expected the expected text
     * @param xml the xml to search
     * @param xpath the xpath to the element to check
     */
    protected void assertEqualsText(String expected, Element xml, String xpath) throws JDOMException {
        final Element element = Xml.selectElement(xml, xpath);
        assertNotNull("No element found at: " + xpath + " in \n" + Xml.getString(xml), element);
        assertEquals(expected, element.getText());
    }

    /**
     * Create an xml params Element in the form most services expect.
     *
     * @param params the params map to convert to Element
     */
    protected Element createParams(Pair<String, String>... params) {
        final Element request = new Element("request");
        for (Pair<String, String> param : params) {
            request.addContent(new Element(param.one()).setText(param.two()));
        }
        return request;
    }

    protected String getStyleSheets() {
        final File file = getWebappDir();

        return new File(file, "xsl/conversion").getPath();
    }

    private File getWebappDir() {
        final String testClassName = AbstractCoreTest.class.getSimpleName();
        File here = new File(AbstractCoreTest.class.getResource(testClassName + ".class").getFile());
        while (!new File(here, "pom.xml").exists()) {
            here = here.getParentFile();
        }

        return new File(here.getParentFile(), "web/src/main/webapp/").getAbsoluteFile();
    }

}
