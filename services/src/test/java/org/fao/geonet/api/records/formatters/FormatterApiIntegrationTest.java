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

package org.fao.geonet.api.records.formatters;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.MockRequestFactoryGeonet;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.ServletContext;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fao.geonet.api.records.formatters.FormatterWidth._100;
import static org.junit.Assert.*;

@ContextConfiguration(inheritLocations = true, locations = "classpath:formatter-test-context.xml")
public class FormatterApiIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    protected MockRequestFactoryGeonet requestFactory;
    @Autowired
    protected SystemInfo systemInfo;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private FormatterApi formatService;
    @Autowired
    private FormatterAdminApi listService;
    private ServiceContext serviceContext;
    private int id;
    private String schema;

    @Before
    public void setUp() throws Exception {
        serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        AbstractMetadata metadata = injectMetadataInDb(getSampleMetadataXml(), serviceContext);
        id = metadata.getId();
        schema = metadata.getDataInfo().getSchemaId();
    }

    @Test
    public void testExec() throws Exception {
        final FormatterAdminApi.FormatterDataResponse formatters =
            listService.listFormatters(null, null, schema, false, false);
        for (FormatterAdminApi.FormatterData formatter : formatters.getFormatters()) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.getSession();
            request.setPathInfo("/eng/blahblah");
            MockHttpServletResponse response = new MockHttpServletResponse();
            final String srvAppContext = "srvAppContext";
            request.getServletContext().setAttribute(srvAppContext, applicationContext);
            JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(srvAppContext);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            formatService.exec("eng", "html", "" + id, null, formatter.getId(), "true", _100, new ServletWebRequest(request, response));

            final String view = response.getContentAsString();
            try {
                assertFalse(formatter.getSchema() + "/" + formatter.getId(), view.isEmpty());
            } catch (Throwable e) {
                e.printStackTrace();
                fail(formatter.getSchema() + " > " + formatter.getId());
            }
            try {
                response = new MockHttpServletResponse();
                formatService.exec("eng", "testpdf", "" + id, null, formatter.getId(), "true", _100,
                    new ServletWebRequest(request, response));
//                Files.write(Paths.get("e:/tmp/view.pdf"), response.getContentAsByteArray());
//                System.exit(0);
            } catch (Throwable t) {
                t.printStackTrace();
                fail(formatter.getSchema() + " > " + formatter.getId());
            }
        }
    }

    @Test
    public void testExecXslt() throws Exception {
        final ServletContext context = _applicationContext.getBean(ServletContext.class);
        MockHttpServletRequest request = new MockHttpServletRequest(context, "GET", "http://localhost:8080/geonetwork/srv/eng/md.formatter");
        request.getSession();
        request.setPathInfo("/eng/md.formatter");

        final String applicationContextAttributeKey = "srv";
        request.getServletContext().setAttribute(applicationContextAttributeKey, _applicationContext);
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final String formatterName = "xsl-test-formatter";
        final URL testFormatterViewFile = FormatterApiIntegrationTest.class.getResource(formatterName + "/view.xsl");
        final Path testFormatter = IO.toPath(testFormatterViewFile.toURI()).getParent();
        final Path formatterDir = this.dataDirectory.getFormatterDir();
        Files.deleteIfExists(formatterDir.resolve("functions.xsl"));
        IO.copyDirectoryOrFile(testFormatter, formatterDir.resolve(formatterName), false);
        IO.copyDirectoryOrFile(testFormatter.getParent().resolve("functions.xsl"), formatterDir, true);
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(applicationContextAttributeKey);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterName, "true", _100, new ServletWebRequest(request, response));
        final String viewXml = response.getContentAsString();
        final Element view = Xml.loadString(viewXml, false);
        assertEqualsText("fromFunction", view, "*//p");
        assertEqualsText("Title", view, "*//div[@class='tr']");
    }
}
