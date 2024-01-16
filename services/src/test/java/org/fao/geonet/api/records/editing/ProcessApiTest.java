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
package org.fao.geonet.api.records.editing;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProcessApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;
    private String seriesUuid;

    private ServiceContext context;

    public static Collection<String[]> data() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[]{"collection-updater", "", "iso19139", "output.xml"});
        return data;
    }

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("/org/fao/geonet/api/records/samples/series-with-three-children.zip");
        importMetadata.invoke();
        seriesUuid = "46fccbdc-d848-47a7-a58d-2aabc21c07cf";
    }

    @Test
    public void checkProcess() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        for (String[] testParameter : data()) {
            String process = testParameter[0];
            String urlParams = testParameter[1];
            String schema = testParameter[2];
            String checkfile = testParameter[3];
            try {
                String url = "/srv/api/processes/" + process
                    + "?uuids=" + seriesUuid + urlParams;
                MvcResult result = mockMvc.perform(get(url)
                        .session(mockHttpSession)
                        .accept(MediaType.ALL_VALUE))
                    .andExpect(status().isOk())
                    .andReturn();

                assertEquals(
                    url,
                    StreamUtils.copyToString(
                            ProcessApiTest.class.getResourceAsStream(
                                String.format("%s-%s-%s",
                                    schema, process, checkfile)
                            ),
                            StandardCharsets.UTF_8)
                        .trim(),
                    result.getResponse().getContentAsString()
                        .replaceAll("\\r\\n?", "\n")
                        .trim()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
