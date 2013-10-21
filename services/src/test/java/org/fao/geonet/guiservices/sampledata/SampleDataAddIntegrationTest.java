package org.fao.geonet.guiservices.sampledata;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test add sample data service
 * User: Jesse
 * Date: 10/16/13
 * Time: 12:47 PM
 */
public class SampleDataAddIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testExec() throws Exception {
        final Add add = new Add();
        ServiceContext context = createServiceContext();

        final Collection<String> schemas = Arrays.asList("iso19115", "fgdc-std", "iso19139", "dublin-core");
        StringBuilder builder = new StringBuilder();

        for (String schema : schemas) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(schema);
        }
        Element params = createParams(Pair.read(Params.SCHEMA, builder.toString()));
        loginAsAdmin(context);

        final Element response = add.exec(params, context);

        assertEquals("true", response.getAttributeValue("status"));
        assertEquals("", response.getAttributeValue("error"));
        assertTrue(Integer.parseInt(response.getAttributeValue("total")) > 0);

        for (String schema : schemas) {
            final String schemaCount = response.getChildText(schema);
            final String responseText = Xml.getString(response);
            assertNotNull("No element: schema count: " + responseText, schemaCount);
            assertTrue("expected schemaCount to be > 0: "+schema, Integer.parseInt(schemaCount) > 0);
        }
    }
}
