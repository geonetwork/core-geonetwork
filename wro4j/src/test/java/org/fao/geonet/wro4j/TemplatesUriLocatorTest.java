package org.fao.geonet.wro4j;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fgravin on 2/17/14.
 */
public class TemplatesUriLocatorTest {
    @Test
    public void testLocate() throws Exception {
        Context.set(Context.standaloneContext());
        final TemplatesUriLocator locator = new TemplatesUriLocator();
        URL path = TemplatesUriLocatorTest.class.getResource("/partials/");
        File file = new File(path.toURI());
        final Resource resource = new Resource();
        resource.setUri(TemplatesUriLocator.URI_PREFIX + file.getPath());

        assertTrue(locator.accept(resource.getUri()));
        final String actual = IOUtils.toString(locator.locate(resource.getUri()));
        String expected = String.format("$templateCache.put('%s/template.html', '<html>\\n<div ng-class=\"test | \\'bold\\'\"></div>\\n</html>\\n');",
            "../.." + file.getPath().replace('\\', '/'));
        assertEquals(expected, actual);
    }
}
