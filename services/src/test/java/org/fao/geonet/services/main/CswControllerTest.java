/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

public class CswControllerTest extends AbstractServiceIntegrationTest {
    private static final Map<String, String> NS = Map.of(
        "csw", "http://www.opengis.net/cat/csw/2.0.2",
        "ows", "http://www.opengis.net/ows",
        "env", "http://www.w3.org/2003/05/soap-envelope");
    private static final String DESCRIBE_RECORD =
        "<csw:DescribeRecord xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\"/>";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SettingManager settingManager;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        settingManager.setValue(Settings.SYSTEM_CSW_ENABLE, true);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void tearDownReadOnly() {
        NodeInfo.setReadOnly(false);
    }

    @Test
    public void kvpRequest() throws Exception {
        mockMvc.perform(get("/srv/eng/csw")
                .param("service", "CSW").param("version", "2.0.2").param("request", "DescribeRecord"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            .andExpect(xpath("/csw:DescribeRecordResponse", NS).exists());
    }

    @Test
    public void xmlRequest() throws Exception {
        mockMvc.perform(post("/srv/eng/csw").contentType("application/xml").content(DESCRIBE_RECORD))
            .andExpect(status().isOk())
            .andExpect(xpath("/csw:DescribeRecordResponse", NS).exists());
    }

    @Test
    public void soapRequest() throws Exception {
        String envelope = "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Body>"
            + DESCRIBE_RECORD + "</env:Body></env:Envelope>";
        mockMvc.perform(post("/srv/eng/csw").contentType("application/soap+xml").content(envelope))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/soap+xml;charset=UTF-8"))
            .andExpect(xpath("/env:Envelope/env:Body/csw:DescribeRecordResponse", NS).exists());
    }

    @Test
    public void invalidRequests() throws Exception {
        mockMvc.perform(get("/srv/eng/csw").param("service", "CSW"))
            .andExpect(xpath("/response/info").string("No 'request' parameter found"));

        mockMvc.perform(get("/srv/eng/csw").param("service", "CSW").param("request", "Transaction"))
            .andExpect(xpath("/response/info").string(startsWith("Not a CSW Discovery operation: Transaction")));

        mockMvc.perform(get("/srv/eng/csw").param("service", "CSW").param("request", "Unknown"))
            .andExpect(xpath("/response/info").string(startsWith("Not a CSW Discovery operation: Unknown")));

        mockMvc.perform(post("/srv/eng/csw").contentType("application/xml").content("<csw:DescribeRecord"))
            .andExpect(status().isBadRequest())
            .andExpect(xpath("/ows:ExceptionReport/ows:Exception/@exceptionCode", NS).string("NoApplicableCode"));
    }

    @Test
    public void cswDisabled() throws Exception {
        settingManager.setValue(Settings.SYSTEM_CSW_ENABLE, false);
        mockMvc.perform(get("/srv/eng/csw").param("service", "CSW").param("request", "DescribeRecord"))
            .andExpect(status().isOk())
            .andExpect(xpath("/response/info").string("CSW is disabled"));
    }

    @Test
    public void publicationInReadOnlyMode() throws Exception {
        NodeInfo.setReadOnly(true);
        mockMvc.perform(post("/srv/eng/csw-publication").contentType("application/xml")
                .content("<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\"/>"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(xpath("/ows:ExceptionReport/ows:Exception/@exceptionCode", NS).string("NoApplicableCode"));

        // Discovery requests can still be sent with POST.
        mockMvc.perform(post("/srv/eng/csw").contentType("application/xml").content(DESCRIBE_RECORD))
            .andExpect(status().isOk())
            .andExpect(xpath("/csw:DescribeRecordResponse", NS).exists());
    }
}
