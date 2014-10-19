package iso19139;

import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.Format;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends AbstractServiceIntegrationTest {
    @Autowired
    private Format formatService;

    @Test
    public void testBasicFormat() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final String mdFile = FullViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        final String xml = Files.toString(new File(mdFile), Constants.CHARSET);

        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(Constants.ENCODING));
        final int id = importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");
        final String view = formatService.exec("eng", "html", "" + id, null, "full_view", null, null, request);

        Files.write(view, new File("e:/tmp/view.html"), Constants.CHARSET);


        final List<?> text = Xml.selectNodes(Xml.loadString(xml, false), "*//text()");
        StringBuilder missingStrings = new StringBuilder();
        for (Object t : text) {
            Text textEl = (Text) t;
            final String requiredText = textEl.getTextNormalize();
            if (!requiredText.isEmpty() && !view.contains(requiredText)) {
                missingStrings.append("\n").append(getXPath(textEl)).append(" -> ").append(requiredText);
            }
        }

        if (missingStrings.length() > 0) {
            throw new AssertionError("The following text elements are missing from the view:" + missingStrings);
        }
    }

    private String getXPath(Content el) {
        String path = "/";
        if (el.getParentElement() != null) {
            path = getXPath(el.getParentElement());
        }
        if (el instanceof Element) {
            return path + ((Element) el).getQualifiedName();
        } else {
            return path + el.getClass().getSimpleName();
        }
    }
}
