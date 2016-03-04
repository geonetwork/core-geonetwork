/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package iso19139;

import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.services.metadata.format.groovy.Handler;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 10/23/2014.
 */
public class IsoMatchersTest extends AbstractFormatterTest {

    @Override
    protected File getTestMetadataFile() throws Exception {
        final URL mdFile = AbstractFullViewFormatterTest.class.getResource("/iso19139/example.xml");
        return new File(mdFile.toURI());
    }

    @Test
    public void testTextMatcher() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final String formatterId = "full_view";
        final Handlers handlers = getHandlers(request, formatterId);
        GPathResult elem = parseXml(
                "<root><gmd:title  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">\n"
                + "  <gmd:PT_FreeText xmlns:gco=\"http://www.isotc211.org/2005/gmd\">\n"
                + "    <gmd:textGroup>\n"
                + "      <gmd:LocalisedCharacterString locale=\"#DE\">GER Citation Title</gmd:LocalisedCharacterString>\n"
                + "    </gmd:textGroup>\n"
                + "  </gmd:PT_FreeText>\n"
                + "</gmd:title></root>", ISO19139Namespaces.GMD, ISO19139Namespaces.GCO);
        final GPathResult titleEl = (GPathResult) elem.getProperty("gmd:title");
        Handler handler = handlers.findHandlerFor(titleEl);

        assertNotNull(handler);
        assertTrue("Expected 'Text Elements' but got '" + handler.getName() + "'", handler.getName().equals("Text Elements"));
        String handlerResult = executeHandler(request, formatterId, titleEl, handler);
        assertTrue(handlerResult, handlerResult.contains("GER Citation Title"));

        elem = parseXml(
                "<root  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" "
                + "xmlns:gco=\"http://www.isotc211.org/2005/gco\" xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">"
                + "<gmd:language>\n"
                + "    <gco:CharacterString>eng</gco:CharacterString>\n"
                + "</gmd:language></root>", ISO19139Namespaces.GMD, ISO19139Namespaces.GCO);
        final GPathResult langEl = (GPathResult) elem.getProperty("gmd:language");
        handler = handlers.findHandlerFor(langEl);

        assertNotNull(handler);
        assertTrue("Expected 'Text Elements' but got '" + handler.getName() + "'", handler.getName().contains("gmd:language"));
        handlerResult = executeHandler(request, formatterId, langEl, handler);
        assertTrue(handlerResult, handlerResult.contains("English"));
        assertTrue(handlerResult, handlerResult.contains("Metadata language"));
    }

}
