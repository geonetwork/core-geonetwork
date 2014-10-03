package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import javax.annotation.Nullable;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListFormattersIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Test
    public void testExec() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final ListFormatters listService = new ListFormatters();
        final ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setValue(FormatterConstants.USER_XSL_DIR, dataDirectory.getWebappDir() + "/xsl");

        listService.init(dataDirectory.getWebappDir(), serviceConfig);
        assertFormattersForSchema("iso19139", listService, 1, "fullView");
        assertFormattersForSchema("iso19139.che", listService, 1, "fullView");
    }

    private void assertFormattersForSchema(String schema, ListFormatters listService, int numFormatters,
                                           String... expectedFormatters) throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        final Element formattersEl = listService.exec(createParams(read("schema", schema)), serviceContext);

        final List<String> formatters = Lists.transform(formattersEl.getChildren("formatter"), new Function() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element) input).getText();
            }
        });

        assertEquals(numFormatters, formatters.size());
        for (String expectedFormatter : expectedFormatters) {
            assertTrue("Expected formatter: "+expectedFormatter, formatters.contains(expectedFormatter));
        }
    }
}