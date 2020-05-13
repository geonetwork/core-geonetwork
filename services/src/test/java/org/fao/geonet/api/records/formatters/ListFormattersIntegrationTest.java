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

package org.fao.geonet.api.records.formatters;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

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
        assertFormattersForSchema(true, "iso19139", listService, "full_view", "datacite", "jsonld");
        assertFormattersForSchema(false, "iso19139", listService, "full_view", "datacite", "jsonld", "xsl-view", "partial_view");
        assertFormattersForSchema(true, "dublin-core", listService, "full_view");
    }

    private void assertFormattersForSchema(boolean publishedOnly, String schema, ListFormatters listService,
                                           String... expectedFormatters) throws Exception {

        final ListFormatters.FormatterDataResponse response = listService.exec(null, null, schema, false, publishedOnly);

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
            assertTrue("Expected formatter: " + expectedFormatter, formatters.contains(schema + "/" + expectedFormatter));
        }
    }
}
