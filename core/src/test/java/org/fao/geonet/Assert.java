package org.fao.geonet;

import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        final List element;
        if (namespaces == null || namespaces.length == 0) {
            element = Xml.selectNodes(xml, xpath);
        } else {
            element = Xml.selectNodes(xml, xpath, Arrays.asList(namespaces));
        }
        assertEquals("Expected 1 element but found " + element.size() + "No element found at: " + xpath + " in \n" + Xml.getString(xml),
                1, element.size());
        String text;
        if (element.get(0) instanceof Element) {
            text = ((Element) element.get(0)).getText();
        } else if (element.get(0) instanceof Attribute) {
            text = ((Attribute) element.get(0)).getValue();
        } else if (element.get(0) instanceof Text) {
            text = ((Text) element.get(0)).getText();
        } else {
            fail("Handling of " + element.get(0).getClass() + " is not yet implemented");
            text = "";
        }
        assertEquals(Xml.getString(xml), expected, text);
    }

}