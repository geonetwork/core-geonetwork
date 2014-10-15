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

import java.util.Arrays;
import java.util.Collections;
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
        serviceConfig.setValue(FormatterConstants.USER_XSL_DIR, dataDirectory.getWebappDir() + "/formatters");

        listService.init(dataDirectory.getWebappDir(), serviceConfig);
        assertFormattersForSchema("iso19139", listService, "full_view",
                "by_package/metadata",
                "by_package/identification",
                "by_package/maintenance",
                "by_package/constraints",
                "by_package/spatial_info",
                "by_package/reference_system",
                "by_package/distribution",
                "by_package/data_quality",
                "by_package/app_schema",
                "by_package/catalog",
                "by_package/content_info",
                "by_package/ext_info");
    }

    private void assertFormattersForSchema(String schema, ListFormatters listService,
                                           String... expectedFormatters) throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        final Element formattersEl = listService.exec(createParams(read("schema", schema)), serviceContext);

        final List<String> formatters = Lists.newArrayList(Lists.transform(formattersEl.getChildren("formatter"), new Function() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element) input).getText();
            }
        }));

        Collections.sort(formatters);
        Arrays.sort(expectedFormatters);

        assertEquals("Expected/Actual: \n" + Arrays.asList(expectedFormatters) + "\n" + formatters,
                expectedFormatters.length, formatters.size());
        for (String expectedFormatter : expectedFormatters) {
            assertTrue("Expected formatter: "+expectedFormatter, formatters.contains(expectedFormatter));
        }
    }
}