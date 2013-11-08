package org.fao.geonet;

import java.util.Collection;

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
}