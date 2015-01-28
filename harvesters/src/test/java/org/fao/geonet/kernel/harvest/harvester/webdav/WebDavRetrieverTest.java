package org.fao.geonet.kernel.harvest.harvester.webdav;

import com.github.sardine.DavResource;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Some tests for {@link org.fao.geonet.kernel.harvest.harvester.webdav.WebDavRetriever}
 * Created by Jesse on 1/24/14.
 */
public class WebDavRetrieverTest {
    @Test
    public void testCalculateBaseURL() throws Exception {
        List<DavResource> resources = new ArrayList<DavResource>(2);
        final DavResource baseResource = Mockito.mock(DavResource.class);
        Mockito.when(baseResource.getPath()).thenReturn("/webdav/");

        final DavResource otherResource = Mockito.mock(DavResource.class);
        Mockito.when(otherResource.getPath()).thenReturn("/webdav/metadata.xml");

        resources.add(baseResource);
        resources.add(otherResource);

        final String baseURL = WebDavRetriever.calculateBaseURL(new AtomicBoolean(), "http://geonetwork.net/webdav/", resources);

        assertEquals("http://geonetwork.net", baseURL);
        assertEquals(1, resources.size());
        assertFalse(resources.contains(baseResource));
        assertTrue(resources.contains(otherResource));
    }
}
