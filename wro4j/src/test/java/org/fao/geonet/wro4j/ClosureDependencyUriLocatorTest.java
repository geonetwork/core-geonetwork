package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;

import static org.fao.geonet.wro4j.ClosureDependencyUriLocator.PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jesse on 1/8/14.
 */
public class ClosureDependencyUriLocatorTest {
    @Test
    public void testLocate() throws Exception {
        final ClosureDependencyUriLocator locator = new ClosureDependencyUriLocator();

        final Resource resource = new Resource();
        resource.setUri(ClosureDependencyUriLocator.URI_PREFIX + "/abc.js@@a");

        assertTrue(locator.accept(resource.getUri()));
        final String actual = IOUtils.toString(locator.locate(resource.getUri()));
        assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/abc.js', ['a'], []);\n", actual);
    }
}
