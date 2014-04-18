package org.fao.geonet.wro4j;

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
        String path = TemplatesUriLocatorTest.class.getResource("/partials/").getFile();
        final Resource resource = new Resource();
        resource.setUri(TemplatesUriLocator.URI_PREFIX + path);

        assertTrue(locator.accept(resource.getUri()));
        final String actual = IOUtils.toString(locator.locate(resource.getUri()));
        assertEquals(String.format("$templateCache.put('%stemplate.html', '<html><div ng-class=\"test | \\'bold\\'\"></div></html>');","../.."+path.replace('\\','/')), actual);
    }
}
