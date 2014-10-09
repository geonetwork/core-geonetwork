package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.List;
import javax.annotation.Nullable;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertFalse;

public class FormatIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private SchemaManager schemaManager;

    @Test
    public void testExec() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = getSampleMetadataXml();
        final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        final int id =  importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);
        final String schema = schemaManager.autodetectSchema(sampleMetadataXml);

        final ListFormatters listService = new ListFormatters();
        final ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setValue(FormatterConstants.USER_XSL_DIR, dataDirectory.getWebappDir() + "/xsl");

        listService.init(dataDirectory.getWebappDir(), serviceConfig);
        final Element formattersEl = listService.exec(createParams(read("schema", schema)), serviceContext);

        final List<String> formatters = Lists.transform(formattersEl.getChildren("formatter"), new Function() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element)input).getText();
            }
        });

        for (String formatter : formatters) {
            final Format formatService = new Format();
            formatService.init(dataDirectory.getWebappDir(), serviceConfig);
            final Element view = formatService.exec(createParams(read("id", id), read("xsl", formatter)), serviceContext);
            view.setName("body");
            Element html = new Element("html").addContent(view);
            assertFalse(html.getChildren().isEmpty());
        }
    }

}