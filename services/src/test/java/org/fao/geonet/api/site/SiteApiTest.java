package org.fao.geonet.api.site;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by francois on 19/06/16.
 */
public class SiteApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Test
    public void getSite() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/api/site")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
//            .andExpect(jsonPath("$.name").value("Lee"));
    }

    @Test
    public void getSettingsSet() throws Exception {

    }

    @Test
    public void getInformation() throws Exception {

    }

    @Test
    public void isCasEnabled() throws Exception {

    }

    @Test
    public void updateStagingProfile() throws Exception {

    }

    @Test
    public void isReadOnly() throws Exception {

    }

    @Test
    public void isIndexing() throws Exception {

    }

    @Test
    public void getSystemInfo() throws Exception {

    }

    @Test
    public void setLogo() throws Exception {

    }

}
