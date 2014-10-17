package iso19139;

import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.Format;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.fao.geonet.domain.Pair.read;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends AbstractServiceIntegrationTest {
    @Autowired
    private DataManager dm;
    @Test
    public void testBasicFormat() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final String mdFile = FullViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        final String xml = Files.toString(new File(mdFile), Constants.CHARSET);

        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(Constants.ENCODING));
        final int id =  importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);

        final Format format = new Format();
        Element params = createParams(read("id", id), read("xsl", "full_view"), read("schema", "iso19139"), read("html", "true"));
        final Element view = format.exec(params, serviceContext);

        Files.write(Xml.getString(view), new File("e:/tmp/view.html"), Constants.CHARSET);
    }
}
