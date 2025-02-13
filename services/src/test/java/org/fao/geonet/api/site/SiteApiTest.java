package org.fao.geonet.api.site;

import junit.framework.Assert;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by francois on 19/06/16.
 */
public class SiteApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SystemInfo systemInfo;

    @Autowired
    StandardPBEStringEncryptor encryptor;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;


    @Test
    public void getSite() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/site")
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is("My GeoNetwork catalogue")));
    }

    @Test
    public void getSettingsSet() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/site/settings")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is("My GeoNetwork catalogue")));
    }


    @Test
    public void updateSettingsFormUrlEncoded() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        encryptor.initialize();

        this.mockMvc.perform(get("/srv/api/site/settings")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is("My GeoNetwork catalogue")));

        String newName = "DataHub";
        this.mockMvc.perform(post("/srv/api/site/settings")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("system/site/name", newName)
                .session(this.mockHttpSession))
            .andExpect(status().is(204))
            .andExpect(content().string("")); // No content should be returned.

        this.mockMvc.perform(get("/srv/api/site/settings")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is(newName)));
    }

    @Test
    public void updateSettingsJson() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        encryptor.initialize();

        this.mockMvc.perform(get("/srv/api/site/settings")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is("My GeoNetwork catalogue")));

        String newName = "JsonDataHub";
        this.mockMvc.perform(post("/srv/api/site/settings")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"system/site/name\": \"" + newName + "\"}")
                .session(this.mockHttpSession))
            .andExpect(status().is(204))
            .andExpect(content().string("")); // No content should be returned.

        this.mockMvc.perform(get("/srv/api/site/settings")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$['system/site/name']", is(newName)));
    }

    @Test
    public void getInformation() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/site/info")
                .accept(MediaType.parseMediaType("application/json"))
                .session(this.mockHttpSession))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.catalogue['data.dataDir']", is(not(isEmptyOrNullString()))))
            .andExpect(jsonPath("$.main['java.version']", is(not(isEmptyOrNullString()))))
            .andExpect(jsonPath("$.database['db.url']", is(not(isEmptyOrNullString()))));
    }

    @Test
    public void isCasEnabled() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // This service returns a boolean, not encapsulated in json, can't use jsonPath
        MvcResult result = this.mockMvc.perform(get("/srv/api/site/info/isCasEnabled")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().equals("false"));
    }

    @Test
    public void updateStagingProfile() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        Assert.assertTrue(systemInfo.getStagingProfile().equals(SystemInfo.Staging.development.toString()));

        this.mockMvc.perform(put("/srv/api/site/info/staging/" + SystemInfo.Staging.production.toString())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        Assert.assertTrue(systemInfo.getStagingProfile().equals(SystemInfo.Staging.production.toString()));
    }

    @Test
    public void isReadOnly() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // This service returns a boolean, not encapsulated in json, can't use jsonPath
        MvcResult result = this.mockMvc.perform(get("/srv/api/site/info/readonly")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().equals("false"));
    }

    @Test
    public void isIndexing() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // This service returns a boolean, not encapsulated in json, can't use jsonPath
        MvcResult result = this.mockMvc.perform(get("/srv/api/site/indexing")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().equals("false"));
    }

    @Test
    public void getSystemInfo() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/site/info/build")
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.stagingProfile", is(not(isEmptyOrNullString()))))
            .andExpect(jsonPath("$.buildDate", is(not(isEmptyOrNullString()))))
            .andExpect(jsonPath("$.version", is(not(isEmptyOrNullString()))))
            .andExpect(jsonPath("$.subVersion", is(not(isEmptyOrNullString()))));
    }

    @Test
    public void getXslTransformations() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/site/info/transforms")
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }


    @Test
    public void setLogo() throws Exception {

    }

}
