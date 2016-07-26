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

import com.google.common.collect.Lists;

import org.fao.geonet.api.records.formatters.groovy.Environment;
import org.fao.geonet.api.records.formatters.groovy.EnvironmentImpl;
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.File;
import java.util.List;

import static org.fao.geonet.api.records.formatters.FormatterWidth._100;

/**
 * @author Jesse on 10/17/2014.
 */
public class XmlViewFormatterTest extends AbstractFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    private IsoLanguageRepository langRepo;
    @Autowired
    private SchemaManager schemaManager;

    @Test
    @SuppressWarnings("unchecked")
    public void testBasicFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        request.addParameter("html", "true");

        final String formatterId = "xml_view";
        FormatterParams fparams = getFormatterFormatterParamsPair
            (request, formatterId).two();
        Environment env = new EnvironmentImpl(fparams, mapper);
        final Functions functions = new Functions(fparams, env);

//        measureFormatterPerformance(request, formatterId);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, _100, new ServletWebRequest(request, response));
        final String view = response.getContentAsString();
//        Files.write(view, new File("e:/tmp/view.html"), Constants.CHARSET);

        final Element xmlEl = Xml.loadString(xml, false);
        final List text = Lists.newArrayList(Xml.selectNodes(xmlEl, "*//node()/text()"));

        StringBuilder missingStrings = new StringBuilder();
        for (Object t : text) {
            final String path;
            final String requiredText;
            if (t instanceof Text) {
                requiredText = ((Text) t).getTextTrim();
                path = getXPath((Content) t).trim();
            } else {
                throw new AssertionError(t.getClass() + " is not handled");
            }
            if (!requiredText.isEmpty() && !view.contains(requiredText)) {
                missingStrings.append("\n").append(path).append(" -> ").append(requiredText);
            }
        }

        if (missingStrings.length() > 0) {
            throw new AssertionError("The following text elements are missing from the view:" + missingStrings);
        }
    }

    private String getXPath(Content el) {
        String path = "";
        if (el.getParentElement() != null) {
            path = getXPath(el.getParentElement());
        }
        if (el instanceof Element) {
            return path + " > " + ((Element) el).getQualifiedName();
        } else {
            return path + " > " + el.getClass().getSimpleName();
        }
    }

    @Override
    protected File getTestMetadataFile() throws Exception {
        return new File(XmlViewFormatterTest.class.getResource("xml_view/test.xml").toURI());
    }
}
