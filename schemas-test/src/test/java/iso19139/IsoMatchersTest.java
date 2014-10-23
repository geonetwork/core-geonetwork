package iso19139;

import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.ReadOnlyTest;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.services.metadata.format.groovy.Handler;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 10/23/2014.
 */
public class IsoMatchersTest extends AbstractFormatterTest {

    @Override
    @Test
    public void runReadOnlyTests() throws InvocationTargetException, IllegalAccessException {
        super.runReadOnlyTests();
    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = FullViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        return new File(mdFile);
    }

    @ReadOnlyTest
    public void testTextMatcher() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final String formatterId = "full_view";
        final Handlers handlers = getHandlers(request, formatterId);
        final GPathResult elem = parseXml(
                "<gmd:title  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">\n"
                + "  <gco:CharacterString/>\n"
                + "  <gco:PT_FreeText xmlns:gco=\"http://www.isotc211.org/2005/gmd\">\n"
                + "    <gco:textGroup>\n"
                + "      <gmd:LocalisedCharacterString locale=\"#DE\">GER Citation Title</gmd:LocalisedCharacterString>\n"
                + "    </gco:textGroup>\n"
                + "  </gco:PT_FreeText>\n"
                + "</gmd:title>", ISO19139Namespaces.GMD, ISO19139Namespaces.GCO);
        Handler handler = handlers.findHandlerFor(elem);

        assertNotNull(handler);
        assertTrue(handler.getName().equals("Text Elements"));
        final String handlerResult = executeHandler(request, formatterId, elem, handler);
        assertTrue(handlerResult, handlerResult.contains("GER Citation Title"));
    }

}
