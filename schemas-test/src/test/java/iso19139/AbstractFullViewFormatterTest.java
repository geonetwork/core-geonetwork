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

import com.google.common.collect.Lists;
import org.fao.geonet.api.records.formatters.AbstractFormatterTest;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterParams;
import org.fao.geonet.api.records.formatters.groovy.Environment;
import org.fao.geonet.api.records.formatters.groovy.EnvironmentImpl;
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.fao.geonet.api.records.formatters.FormatterWidth._100;

/**
 * @author Jesse on 10/17/2014.
 */
public abstract class AbstractFullViewFormatterTest extends AbstractFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;

    @SuppressWarnings("unchecked")
    protected void testPrintFormat() throws Exception {
        final FormatType formatType = FormatType.testpdf;

        Format format = new Format(formatType).invoke();
        Functions functions = format.getFunctions();
        String view = format.getView();

        List<String> excludes = excludes();

        final Element xmlEl = Xml.loadString(xml, false);
        final List text = Lists.newArrayList(Xml.selectNodes(xmlEl, "*//node()[not(@codeList)]/text()"));
        text.addAll(Lists.newArrayList(Xml.selectNodes(xmlEl, "*//node()[@codeList]/@codeListValue")));

        StringBuilder missingStrings = new StringBuilder();
        for (Object t : text) {
            final String path;
            final String requiredText;
            if (t instanceof Text) {
                requiredText = ((Text) t).getTextTrim();
                path = getXPath((Content) t).trim();
            } else if (t instanceof Attribute) {
                Attribute attribute = (Attribute) t;
                final String codelist = attribute.getParent().getAttributeValue("codeList");
                final String code = attribute.getValue();
                requiredText = functions.codelistValueLabel(codelist, code);
                path = getXPath(attribute.getParent()).trim() + "> @codeListValue";
            } else {
                throw new AssertionError(t.getClass() + " is not handled");
            }
            if (!requiredText.isEmpty() && !view.contains(requiredText)) {
                if (!excludes.contains(path.trim())) {
                    missingStrings.append("\n").append(path).append(" -> ").append(requiredText);
                }
            }
        }

        if (missingStrings.length() > 0) {
            throw new AssertionError("The following text elements are missing from the view:" + missingStrings);
        }
    }

    protected List<String> excludes() {
        return Lists.newArrayList();
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
        final URL mdFile = AbstractFullViewFormatterTest.class.getResource("/iso19139/example.xml");
        return new File(mdFile.toURI());
    }

    protected class Format {
        private FormatType formatType;
        private Functions functions;
        private String view;
        private String requestLanguage = "eng";
        ;

        public Format(FormatType formatType) throws Exception {
            this.formatType = formatType;
        }

        public Functions getFunctions() {
            return functions;
        }

        public String getView() {
            return view;
        }

        public Format invoke() throws Exception {
            view = null;
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.getSession();
            MockHttpServletResponse response = new MockHttpServletResponse();
//            measureFormatterPerformance(request, formatterId);

            final String formatterId = "full_view";
            FormatterParams fparams = getFormatterFormatterParamsPair(request, formatterId).two();
            Environment env = new EnvironmentImpl(fparams, mapper);
            functions = new Functions(fparams, env);

//            formatService.exec("eng", FormatType.html.name(), "" + id, null, formatterId, "true", false, request, response);
            formatService.exec(getRequestLanguage(), formatType.name(), "" + id, null, formatterId, "true", false, _100,
                new ServletWebRequest(request, response));
            view = response.getContentAsString();
//            Files.write(view, new File("e:/tmp/view.html"), Constants.CHARSET);

            return this;
        }

        public String getRequestLanguage() {
            return requestLanguage;
        }

        public void setRequestLanguage(String requestLanguage) {
            this.requestLanguage = requestLanguage;
        }
    }
}
