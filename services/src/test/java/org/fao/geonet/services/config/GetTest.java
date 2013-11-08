package org.fao.geonet.services.config;

import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

/**
 * Test the get config service
 * User: Jesse
 * Date: 11/6/13
 * Time: 12:15 PM
 */
public class GetTest extends AbstractServiceIntegrationTest {
    @Test
    public void testExec() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final Get getService = new Get();

        final Element result = getService.exec(createParams(), context);

        assertEqualsText("false", result, "system/csw/metadataPublic");
        assertEqualsText("", result, "system/csw/contactId");
        assertEquals("system/csw/contactId", Xml.selectElement(result, "system/csw/contactId").getAttributeValue("name"));
        assertEquals("system/csw", Xml.selectElement(result, "system/csw").getAttributeValue("name"));
        assertEqualsText("true", result, "system/csw/enable");
    }
}
