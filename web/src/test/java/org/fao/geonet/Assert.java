package org.fao.geonet;

import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Useful extensions to Junit TestCase.
 *
 * @author heikki doeleman
 */
public final class Assert extends junit.framework.TestCase {

    /**
     * Just to prevent junit.framework.AssertionFailedError: No tests found in org.fao.geonet.test.TestCase.
     */
    public void testPreventAssertionFailedError() {}

    /**
     * Whether something is in a collection.
     *
     * @param msg
     * @param o
     * @param c
     */
    public static void assertContains(String msg, Object o, Collection<?> c) {
        for(Object in : c) {
            if(o.equals(in)) {
                return;
            }
        }
        fail(msg);
    }

    /**
     * Check if an element exists and if it has the expected test.
     *
     * @param expected the expected text
     * @param xml      the xml to search
     * @param xpath    the xpath to the element to check
     * @param namespaces the namespaces required for xpath
     */
    public static void assertEqualsText(String expected, Element xml, String xpath, Namespace... namespaces) throws JDOMException {
        final Element element;
        if (namespaces == null || namespaces.length == 0) {
            element = Xml.selectElement(xml, xpath);
        } else {
            element = Xml.selectElement(xml, xpath, Arrays.asList(namespaces));
        }
        assertNotNull("No element found at: " + xpath + " in \n" + Xml.getString(xml), element);
        assertEquals(expected, element.getText());
    }
    /**
     * Look up the webapp directory.
     *
     * @return
     */
    public static String getWebappDir(Class<?> cl) {
        File here = getClassFile(cl);
        while (!new File(here, "pom.xml").exists() && !new File(here.getParentFile(), "web/src/main/webapp/").exists()) {
//            System.out.println("Did not find pom file in: "+here);
            here = here.getParentFile();
        }

        return new File(here.getParentFile(), "web/src/main/webapp/").getAbsolutePath()+"/";
    }

    private static File getClassFile(Class<?> cl) {
        final String testClassName = cl.getSimpleName();
        return new File(cl.getResource(testClassName + ".class").getFile());
    }

}