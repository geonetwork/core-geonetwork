package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.IO;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the UUID template filters
 * @author Jesse on 4/27/2015.
 */
public class FilterUUIDTest extends AbstractTemplateParserTest {

    @Test
    public void testProcess() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("uuid-filter-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));
        Map<String, Object> model = Maps.newHashMap();
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);

        parseTree.render(context);

        String uuid = FilterGenerateUUID.LAST_UUID.get();
        assertNotNull(uuid);
        String expected = String.format("<html><div id=\"%s\" val=\"%s\"></div></html>", uuid, uuid);

        assertEquals(expected, result.toString().replaceAll("\n*\r*", ""));
    }
}