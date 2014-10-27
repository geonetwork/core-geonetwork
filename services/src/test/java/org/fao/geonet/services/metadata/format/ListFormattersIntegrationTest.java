package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListFormattersIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private ListFormatters listService;

    @Test
    public void testExec() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setValue(FormatterConstants.USER_XSL_DIR, dataDirectory.getWebappDir() + "/formatters");

        listService.init(dataDirectory.getWebappDir(), serviceConfig);
        assertFormattersForSchema("iso19139", listService, "full_view",
                "package/1_metadata",
                "package/2_identification",
                "package/3_maintenance",
                "package/4_constraints",
                "package/5_spatial_info",
                "package/6_reference_system",
                "package/7_distribution",
                "package/8_data_quality",
                "package/9_app_schema",
                "package/10_portrayal_catalog",
                "package/11_content_info",
                "package/12_extension_info");
        assertFormattersForSchema("dublin-core", listService, "full_view");
    }

    private void assertFormattersForSchema(String schema, ListFormatters listService,
                                           String... expectedFormatters) throws Exception {

        final ListFormatters.FormatterDataResponse response = listService.exec(null, null, schema, false);

        final List<String> formatters = Lists.newArrayList(Lists.transform(response.getFormatters(), new Function<ListFormatters.FormatterData, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ListFormatters.FormatterData input) {
                return input.getSchema() + "/" + input.getId();
            }
        }));

        Collections.sort(formatters);
        Arrays.sort(expectedFormatters);

        assertEquals("Expected/Actual: \n" + Arrays.asList(expectedFormatters) + "\n" + formatters,
                expectedFormatters.length, formatters.size());
        for (String expectedFormatter : expectedFormatters) {
            assertTrue("Expected formatter: "+expectedFormatter, formatters.contains(schema + "/" + expectedFormatter));
        }
    }
}