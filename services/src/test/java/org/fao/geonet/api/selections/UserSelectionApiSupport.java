/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.selections;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.collections4.CollectionUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserMetadataSelectionList;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserMetadataSelectionListRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.fao.geonet.api.selections.UserSelectionApi.SESSION_COOKIE_NAME;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * this is specifically for UserSelectionApiTest to make that class just have the tests in it
 * and this class to have support methods.
 *
 * This makes the test cases simpler.
 *
 * 1. inserts 3 metadata records (uuid1,uuid2, uuid3)
 * 2. creates 3 users (user1, user2, user3)
 *
 */
public abstract class UserSelectionApiSupport extends AbstractServiceIntegrationTest {
    @Autowired
    UserSelectionApi userSelectionApi;

    @Autowired
    UserMetadataSelectionListRepository metadataSelectionListRepository;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    DataManager dataManager;

    MockMvc mockMvc;

    MockHttpSession mockHttpSession;

    ServiceContext context;

    static String apiBaseURL = "/srv/api/userselection";

    ///-- UUIDS of metadata documents
    String uuid1;
    String uuid2;
    String uuid3;

    //-- USERS
    User user1;
    User user2;
    User user3;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();
        context = createServiceContext();

        loginAsAdmin(context);

        uuid1 = insertSampleMetadata();
        uuid2 = insertSampleMetadata();
        uuid3 = insertSampleMetadata();

        user1 = _userRepo.save(newUser(1));
        user2 = _userRepo.save(newUser(2));
        user3 = _userRepo.save(newUser(3));
    }

    //creates a user
    User newUser(int id) {
        String val = String.format("%04d", id);
        User user = new User().setName("name" + val).setUsername("username" + val);
        user.getSecurity().setPassword("1234567");
        return user;
    }

    /**
     * puts a metadata document in the database, returns the UUID of it.
     */
    String insertSampleMetadata() throws  Exception {
        final Element sampleMetadataXml = getSampleMetadataXml();
        String uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        final Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml).setUuid(uuid);
        metadata.getDataInfo().setRoot(sampleMetadataXml.getQualifiedName()).setSchemaId(schema).setType(MetadataType.METADATA);
        metadata.getDataInfo().setPopularity(1000);
        metadata.getSourceInfo().setOwner(1).setSourceId(source);
        metadata.getHarvestInfo().setHarvested(false);


        int id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, IndexingMode.none, false, UpdateDatestamp.NO,
            false, false).getId();
        return uuid;
    }


    /**
     * uses the webapi to create a UserMetadataSelectionList.
     * The result is validated against what was inserted.
     */
    public Pair<UserMetadataSelectionList,String> create(MockHttpSession session,
                                                         String cookieValue,
                                                         String name, UserMetadataSelectionList.ListType listType, String[] uuids)
        throws Exception {

        // create the x-www-form-urlencoded for the parameters
        StringBuilder requestContent = new StringBuilder("name="+name+"&listType="+listType.toString());
        Arrays.stream(uuids).forEach(x->requestContent.append("&metadataUuids="+x));

        // actually call api
        MockHttpServletRequestBuilder requestBuilder =   post(apiBaseURL)
            .content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }

        ResultActions result =  this.mockMvc.perform(requestBuilder);

        // get the result of the api call as string (json text) and convert to an actual object
        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        UserMetadataSelectionList resultListObj = objectMapper.readValue(jsonStr,UserMetadataSelectionList.class);

        //valid the response
        assertEquals(name, resultListObj.getName());
        assertEquals(false, resultListObj.getIsPublic());
        assertEquals(listType, resultListObj.getListType());

        //has all the same metadata uuids
        assertTrue(CollectionUtils.isEqualCollection( resultListObj.getSelections().stream()
            .map(x->x.getMetadataUuid()).collect(Collectors.toList()), Arrays.asList(uuids)));

        UserSession userSession = ApiUtils.getUserSession(mockHttpSession);
        String userId = userSession.getUserId();
        String sessionId;
        if (result.andReturn().getResponse().getCookie(SESSION_COOKIE_NAME) != null) {
            //cookie was set in response
            sessionId =result.andReturn().getResponse().getCookie(SESSION_COOKIE_NAME).getValue();
        }
        else {
            sessionId = cookieValue;// value sent in request
        }

        // don't have a sessionId AND userid
        assertTrue(resultListObj.getSessionId() == null || resultListObj.getUser() == null);
        //verify user or session is correct
        if (userId != null) {
            assertEquals(Integer.toString(resultListObj.getUser().getId()) , userId);
        }
        else {
            assertEquals(sessionId,resultListObj.getSessionId());
            assertNotNull(sessionId);
        }



        return Pair.read(resultListObj,sessionId);
    }

    //calls the API to get all lists for the user  (cf UserSelectionApi#getSelectionLists)
    UserMetadataSelectionList[] getAllLists(MockHttpSession session, String cookieValue ) throws  Exception {

        MockHttpServletRequestBuilder requestBuilder = get(apiBaseURL)
            //.content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }


        ResultActions result =  this.mockMvc.perform(requestBuilder);

        // get the result of the api call as string (json text) and convert to an actual object
        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        UserMetadataSelectionList[] resultListObj = objectMapper.readValue(jsonStr,UserMetadataSelectionList[].class);
        return resultListObj;
    }

    //Gets a single list (by id) from the API (cf UserSelectionApi#getSelectionList)
    UserMetadataSelectionList getList(MockHttpSession session, String cookieValue, int id) throws Exception {


        MockHttpServletRequestBuilder requestBuilder =        get(apiBaseURL+"/"+id)
            //.content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }

        ResultActions result =  this.mockMvc.perform(requestBuilder);

        // get the result of the api call as string (json text) and convert to an actual object
        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        UserMetadataSelectionList resultListObj = objectMapper.readValue(jsonStr,UserMetadataSelectionList.class);
        return resultListObj;
    }

    UserMetadataSelectionList setstatus(MockHttpSession session, String cookieValue,
                                     int id,
                                     boolean isPublic) throws Exception {
        StringBuilder requestContent = new StringBuilder("public="+Boolean.toString(isPublic));

        MockHttpServletRequestBuilder requestBuilder =          put(apiBaseURL+"/"+id+"/status")
            .content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }


        ResultActions result =  this.mockMvc.perform(requestBuilder);

        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        UserMetadataSelectionList resultListObj = objectMapper.readValue(jsonStr,UserMetadataSelectionList.class);

        return  resultListObj;
    }

    Boolean deleteItem(MockHttpSession session, String cookieValue, int id) throws Exception {

        MockHttpServletRequestBuilder requestBuilder = delete(apiBaseURL+"/"+id)
            //   .content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }

        ResultActions result =  this.mockMvc.perform(requestBuilder);

        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonStr,Boolean.class);
    }


    UserMetadataSelectionList update(MockHttpSession session, String cookieValue,
                                     int id,
                                     String name,
                                     String[] uuids,
                                     UserSelectionApi.ActionType actionType) throws Exception {
        StringBuilder requestContent = new StringBuilder("name="+name+"&action="+actionType.toString());
        Arrays.stream(uuids).forEach(x->requestContent.append("&metadataUuids="+x));

        MockHttpServletRequestBuilder requestBuilder =   put(apiBaseURL+"/"+id)
            .content(requestContent.toString())
            .session(session)
            .contentType("application/x-www-form-urlencoded")
            .accept(MediaType.parseMediaType(API_JSON_EXPECTED_ENCODING));
        if (cookieValue !=null) {
            requestBuilder.cookie(new Cookie(SESSION_COOKIE_NAME,cookieValue));
        }


        ResultActions result =  this.mockMvc.perform(requestBuilder);

        String jsonStr= result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        UserMetadataSelectionList resultListObj = objectMapper.readValue(jsonStr,UserMetadataSelectionList.class);

        return  resultListObj;
    }


    //list should not contain specified item (by id)
    public void doesNotContain(List<UserMetadataSelectionList> list1, UserMetadataSelectionList item) {
        boolean contains= list1.stream()
            .anyMatch(x->x.getId() == item.getId());
        assertFalse(contains);
    }

    //given two lists of UserMetadataSelectionList check that they are the same
    // cf areSame(UserMetadataSelectionList,UserMetadataSelectionList)
    public void areSame(List<UserMetadataSelectionList> list1, List<UserMetadataSelectionList> list2) {
        assertEquals(list1.size(),list2.size());

        list1.sort(Comparator.comparing(UserMetadataSelectionList::getId));
        list2.sort(Comparator.comparing(UserMetadataSelectionList::getId));

        for (int t=0;t<list1.size();t++){
            UserMetadataSelectionList e1 = list1.get(t);
            UserMetadataSelectionList e2 = list2.get(t);
            areSame(e1,e2);
        }
    }

    public void contains(List<UserMetadataSelectionList> list, int id){
       assertTrue(list.stream()
            .anyMatch(x->x.getId()==id));
    }

    public void notContains(List<UserMetadataSelectionList> list, int id){
        assertFalse(list.stream()
            .anyMatch(x->x.getId()==id));
    }

    //Tests that two UserMetadataSelectionLists are the same.
    public void areSame(UserMetadataSelectionList l1, UserMetadataSelectionList l2) {
        assertEquals(l1.getId(),l2.getId());
        assertEquals(l1.getName(),l2.getName());
        assertEquals(l1.getIsPublic(),l2.getIsPublic());
        assertEquals(l1.getCreateDate(),l2.getCreateDate());
        assertEquals(l1.getChangeDate(),l2.getChangeDate());
        assertEquals(l1.getListType(),l2.getListType());

        if (l1.getUser() != null) {
            assertNotNull(l2.getUser());
            assertEquals(l1.getUser().getId(),l2.getUser().getId());
        }
        else {
            assertEquals(l1.getSessionId(),l2.getSessionId());
        }

        assertTrue(CollectionUtils.isEqualCollection(
            l1.getSelections().stream().map(x->x.getMetadataUuid()).collect(Collectors.toList()),
            l2.getSelections().stream().map(x->x.getMetadataUuid()).collect(Collectors.toList())));
    }

    public void areSameUuids(List<String> list1, UserMetadataSelectionList item){
        List<String> list2 = item.getSelections().stream()
                .map(x->x.getMetadataUuid())
                    .collect(Collectors.toList());
        Collections.sort(list1);
        Collections.sort(list2);

       assertEquals(list1,list2);
    }



}
