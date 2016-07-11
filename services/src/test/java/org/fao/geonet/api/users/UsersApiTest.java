package org.fao.geonet.api.users;


import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by jose on 11/07/16.
 */
public class UsersApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;


    @Before
    public void setUp() {
        // Setup session
        mockHttpSession = new MockHttpSession();

        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);

        UserSession userSession = (UserSession) mockHttpSession.getAttribute(Jeeves.Elem.SESSION);
        if (userSession == null) {
            userSession = new UserSession();
            userSession.loginAs(admin);

            mockHttpSession.setAttribute(Jeeves.Elem.SESSION, userSession);
            userSession.setsHttpSession(mockHttpSession);
        }
    }


    @Test
    public void getUsers() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MvcResult result =this.mockMvc.perform(get("/api/users")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json")).andReturn();

        String content = result.getResponse().getContentAsString();
        JSONArray json = new JSONArray(content);
        Assert.assertTrue(json.length() >= 1);
    }

    @Test
    public void getNonExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/api/users/22")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void getExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/api/users/1")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }


    @Test
    public void deleteExistingUser() throws Exception {
        User user = new User();
        user.setUsername("editor");
        user.setProfile(Profile.Editor);
        user.setEnabled(true);
        user.getEmailAddresses().add("test@mail.com");
        _userRepo.save(user);

        user = _userRepo.findOneByUsername("editor");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/api/users/" + user.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void deleteNonExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/api/users/22")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType("application/json"));
    }
}
