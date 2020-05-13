package org.fao.geonet.api.reports;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for ReportApi.
 *
 * @author Jose García
 */
public class ReportApiTests extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void getDataDownloads() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/reports/datadownloads")
                .session(mockHttpSession)
                .param("dateFrom", "2017-01-01")
                .param("dateTo", "2017-02-01")
                .param("groups", "1")
                .accept(MediaType.parseMediaType("text/x-csv")))
                .andExpect(status().isOk());
    }

    @Test
    public void getDataUploads() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/reports/datauploads")
                .session(mockHttpSession)
                .param("dateFrom", "2017-01-01")
                .param("dateTo", "2017-02-01")
                .param("groups", "1")
                .accept(MediaType.parseMediaType("text/x-csv")))
                .andExpect(status().isOk());
    }

    @Test
    public void getUsers() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/reports/users")
                .session(mockHttpSession)
                .param("dateFrom", "2017-01-01")
                .param("dateTo", "2017-02-01")
                .accept(MediaType.parseMediaType("text/x-csv")))
                .andExpect(status().isOk());
    }


    @Test
    public void getMetadataUpdated() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/reports/metadataupdated")
                .session(mockHttpSession)
                .param("dateFrom", "2017-01-01")
                .param("dateTo", "2017-02-01")
                .param("groups", "1")
                .accept(MediaType.parseMediaType("text/x-csv")))
                .andExpect(status().isOk());
    }

    @Test
    public void getMetadataInternal() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/reports/metadatainternal")
                .session(mockHttpSession)
                .param("dateFrom", "2017-01-01")
                .param("dateTo", "2017-02-01")
                .param("groups", "1")
                .accept(MediaType.parseMediaType("text/x-csv")))
                .andExpect(status().isOk());
    }
}
