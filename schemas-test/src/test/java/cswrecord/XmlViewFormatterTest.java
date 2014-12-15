package cswrecord;

import com.google.common.collect.Lists;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class XmlViewFormatterTest extends AbstractFormatterTest {

    @Test
    public void testBasicFormat() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final String formatterId = "xml_view";

//        measureFormatterPerformance(request, formatterId);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, request, response);
        String view = response.getContentAsString();
//        Files.write(view, new File("e:/tmp/view.html"), Constants.CHARSET);
        view = view.replaceAll("\\s+", " ");

        List<String> excludes = Lists.newArrayList(
        );

        final Element xmlEl = Xml.loadString(xml, false);
        final List text = Lists.newArrayList(Xml.selectNodes(xmlEl, "*//text()"));

        StringBuilder missingStrings = new StringBuilder();
        for (Object t : text) {
            Text textEl = (Text) t;
            final String requiredText = escapeXmlText(textEl.getTextTrim());
            if (!requiredText.isEmpty() && !view.contains(requiredText)) {
                final String path = getXPath(textEl).trim();
                if (!excludes.contains(path)) {
                    missingStrings.append("\n").append(path).append(" -> ").append(requiredText);
                }
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
    protected File getTestMetadataFile() {
        final String mdFile = XmlViewFormatterTest.class.getResource("/cswrecord/example.xml").getFile();
        return new File(mdFile);
    }
}
