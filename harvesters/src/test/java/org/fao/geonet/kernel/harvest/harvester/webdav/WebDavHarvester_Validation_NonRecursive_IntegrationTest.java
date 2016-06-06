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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import com.google.common.io.CharStreams;

import org.fao.geonet.kernel.harvest.AbstractHarvesterIntegrationTest;
import org.fao.geonet.MockCloseableHttpResponse;
import org.fao.geonet.MockRequestFactoryGeonet;
import org.jdom.Element;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration test for the WebDav Harvester harvesting from WebDav.
 *
 * Created by Jesse on 1/12/14.
 */
public class WebDavHarvester_Validation_NonRecursive_IntegrationTest extends AbstractHarvesterIntegrationTest {

    private static final String BASE_PATH = "/webdav/";
    private static final String HOST = "http://webdav_harvester_test.com";
    private static final String WEB_DAV_URL = HOST + BASE_PATH;
    private static final String START_DATE = "Thu, 21 Jan 2014 20:01:22 GMT";
    private static final String MODIFIED_DATE = "Thu, 23 Jan 2014 20:01:22 GMT";

//    @Autowired
//    private WebDavHarvester _harvester;

    public WebDavHarvester_Validation_NonRecursive_IntegrationTest() {
        super("webdav");
    }

    @Override
    protected int getExpectedTotalFound() {
        return 3;
    }

    @Override
    protected int getExpectedAdded() {
        return 1;
    }

    @Override
    protected int getExpectedDoesNotValidate() {
        return 1;
    }

    @Override
    protected int getExpectedUnknownSchema() {
        return 1;
    }

    @Override
    protected void mockHttpRequests(MockRequestFactoryGeonet bean) throws Exception {
        boolean isRecursive = isRecursive();
        final WebDavDescriptor folder = new WebDavDescriptor(true, "folder/", "folder", START_DATE, isRecursive)
            .add(new WebDavDescriptor(false, "lvl2Valid.xml", "validMd.xml", START_DATE, isRecursive))
            .add(new WebDavDescriptor(false, "lvl2InValid.xml", "invalidMd.xml", START_DATE, isRecursive));

        final WebDavDescriptor root = new WebDavDescriptor(true, BASE_PATH, "root", START_DATE, true)
            .add(folder)
            .add(new WebDavDescriptor(false, "validMd.xml", "validMd.xml", START_DATE, true))
            .add(new WebDavDescriptor(false, "invalidMd.xml", "invalidMd.xml", START_DATE, true))
            .add(new WebDavDescriptor(false, "badMd.xml", "badMd.xml", START_DATE, true));

        root.registerMockRequest(bean, "");
    }


    @Override
    protected void customizeParams(Element node) {
        Element site = node.getChild("site");
        Element opt = node.getChild("options");
        final Element content = node.getChild("content");
        content.getChild("validate").setText("" + onlyValid());

        site.addContent(new Element("url").setText(WEB_DAV_URL));
        opt.addContent(new Element("recurse").setText("" + isRecursive()));
        opt.addContent(new Element("subtype").setText("webdav"));
    }

    protected boolean isRecursive() {
        return false;
    }

    protected boolean onlyValid() {
        return true;
    }

    private static class WebDavDescriptor {
        private static final String WEBDAV_DIR_TEMPLATE_XML = "webdav-dir-template.xml";
        private static final String WEBDAV_FILE_TEMPLATE_XML = "webdav-file-template.xml";
        boolean directory;
        String href;
        String fileName;
        String lastModifiedDate;
        boolean expectedToBeCalled;
        List<WebDavDescriptor> children = new ArrayList<WebDavDescriptor>();

        private WebDavDescriptor(boolean directory, String href, String fileName, String lastModifiedDate, boolean expectedToBeCalled) {
            this.directory = directory;
            this.href = href;
            this.fileName = fileName;
            this.lastModifiedDate = lastModifiedDate;
            this.expectedToBeCalled = expectedToBeCalled;
        }

        WebDavDescriptor add(WebDavDescriptor... children) {
            this.children.addAll(Arrays.asList(children));
            return this;
        }

        int registerMockRequest(MockRequestFactoryGeonet factory, String parentHref) throws Exception {
            if (directory) {
                registerDirectory(factory, parentHref);
                return -1;
            } else {
                return registerFile(factory, parentHref);
            }
        }

        private int registerFile(MockRequestFactoryGeonet factory, String parentHref) throws Exception {
            final byte[] md1Data = getUnmodifiedResource(fileName).replace("@@fileId@@", parentHref + href).getBytes("UTF-8");
            MockCloseableHttpResponse md1Response = new MockCloseableHttpResponse(200, "OK", md1Data);
            factory.registerRequest(expectedToBeCalled, new URI(HOST + parentHref + href), md1Response);
            return md1Data.length;
        }

        private void registerDirectory(MockRequestFactoryGeonet factory, String parentHref) throws Exception {
            StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns0=\"DAV:\">\n");

            builder.append(getWebDavSnippet(parentHref, WEBDAV_DIR_TEMPLATE_XML, -1));
            for (WebDavDescriptor child : children) {
                int contentLength = child.registerMockRequest(factory, parentHref + href);
                String template;
                if (child.directory) {
                    template = WEBDAV_DIR_TEMPLATE_XML;
                } else {
                    template = WEBDAV_FILE_TEMPLATE_XML;
                }
                builder.append(child.getWebDavSnippet(parentHref + href, template, contentLength));
            }

            builder.append("</D:multistatus>");

            final byte[] bytes = builder.toString().getBytes("UTF-8");
            MockCloseableHttpResponse md1Response = new MockCloseableHttpResponse(207, "Multi-Response", bytes);
            factory.registerRequest(expectedToBeCalled, new URI(HOST + parentHref + href), md1Response);
        }

        private String getWebDavSnippet(String parentHref, String xmlFile, int contentLength) throws Exception {
            final String s = getUnmodifiedResource(xmlFile);

            return s.replace("@@href@@", parentHref + href)
                .replace("@@modDate@@", lastModifiedDate)
                .replace("@@contentLength@@", "" + contentLength);
        }


        private String getUnmodifiedResource(String xmlFile) throws Exception {
            final InputStream resourceAsStream = WebDavHarvester_Validation_NonRecursive_IntegrationTest.class.getResourceAsStream(xmlFile);
            return CharStreams.toString(new InputStreamReader(resourceAsStream, "UTF-8"));
        }
    }
}
