package org.fao.geonet.test;

import java.util.Collection;

/**
 * Useful extensions to Junit TestCase.
 *
 * @author heikki doeleman
 */
public class TestCase extends junit.framework.TestCase {

    /**
     * Just to prevent junit.framework.AssertionFailedError: No tests found in org.fao.geonet.test.TestCase.
     */
    public void testNothing() {}

    /**
     * Whether something is in a collection.
     *
     * @param msg
     * @param o
     * @param c
     */
    public static void assertContains(String msg, Object o, Collection c) {
        for(Object in : c) {
            if(o.equals(in)) {
                return;
            }
        }
        fail(msg);
    }

    /**
     * No-arg constructor to enable serialization. This method is not intended to be used by mere mortals without
     * calling setName().
     */
    public TestCase() {
        super();
    }
    /**
     * Constructs a test case with the given name.
     */
    public TestCase(String name) {
        super(name);
    }
    /**
     * Sets the name of a TestCase.
     * @param name the name to set
     */
    public void setName(String name) {
        super.setName(name);
    }
}