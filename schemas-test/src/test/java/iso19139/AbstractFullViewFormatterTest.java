package iso19139;

import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.guiservices.metadata.GetRelated;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentImpl;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.util.List;

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
    protected File getTestMetadataFile() {
        final String mdFile = AbstractFullViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        return new File(mdFile);
    }

    protected class Format {
        private FormatType formatType;
        private Functions functions;
        private String view;
        private String requestLanguage = "eng";;

        public Format(FormatType formatType) throws Exception {
            this.formatType = formatType;
            GetRelated related = Mockito.mock(GetRelated.class);
            Element relatedXml = Xml.loadFile(AbstractFullViewFormatterTest.class.getResource("relations.xml"));
            Mockito.when(related.getRelated(Mockito.<ServiceContext>any(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyInt(), Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(relatedXml);
            _applicationContext.getBeanFactory().registerSingleton("getRelated", related);


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
            MockHttpServletResponse response = new MockHttpServletResponse();
//            measureFormatterPerformance(request, formatterId);

            final String formatterId = "full_view";
            FormatterParams fparams = getFormatterFormatterParamsPair(request, formatterId).two();
            Environment env = new EnvironmentImpl(fparams, mapper);
            functions = new Functions(fparams, env);

//            formatService.exec("eng", FormatType.html.name(), "" + id, null, formatterId, "true", false, request, response);
            formatService.exec(getRequestLanguage(), formatType.name(), "" + id, null, formatterId, "true", false, request, response);
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
