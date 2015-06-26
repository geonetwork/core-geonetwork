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
import org.springframework.web.context.request.ServletWebRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.services.metadata.format.FormatterWidth._100;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends AbstractFormatterTest {

    @Test
    public void testBasicFormat() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final String formatterId = "full_view";

//        measureFormatterPerformance(request, formatterId);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, _100, new ServletWebRequest(request, response));
        final String view = response.getContentAsString();
//        Files.write(view, new File("e:/tmp/view.html"), Constants.CHARSET);

        List<String> excludes = new ArrayList<>();

        final Element xmlEl = Xml.loadString(xml, false);
        final List text = Lists.newArrayList(Xml.selectNodes(xmlEl, "*//text()"));

        StringBuilder missingStrings = new StringBuilder();
        for (Object t : text) {
            Text textEl = (Text) t;
            final String requiredText = textEl.getTextTrim();
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
        final String mdFile = FullViewFormatterTest.class.getResource("/cswrecord/example.xml").getFile();
        return new File(mdFile);
    }
}
