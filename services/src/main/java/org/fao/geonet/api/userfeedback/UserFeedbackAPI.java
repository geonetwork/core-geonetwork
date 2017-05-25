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

package org.fao.geonet.api.userfeedback;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fao.geonet.api.API;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.utils.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping(value = {
        "/api",
        "/api/" + API.VERSION_0_1
})
@Api(value = "userfeedback", tags = "userfeedback")
@Controller("userfeedback")
public class UserFeedbackAPI {

    public static final String API_PARAM_CSW_SERVICE_IDENTIFIER = "Service identifier";
    public static final String API_PARAM_CSW_SERVICE_DETAILS = "Service details";
    
    // GET
    @ApiOperation(
            value = "Finds a list of usercomment records",
            notes = "Finds a list of usercomment records, filtered by: target={uuid}"
                    + " any={searchstring} "
                    + " From To user={userid} Orderby Sortorder Published Ownergroup "
                    + " (filter feedbacks on metadata owned by group x) ",
                    nickname = "getUserComments")
    @RequestMapping(
            value = "/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserFeedback> getUserComments(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComments");
        
        
        return list;
    }

    @ApiOperation(
            value = "Finds a specific usercomment",
            notes = "Finds a specific usercomment",
            nickname = "getUserComment")
    @RequestMapping(
            value = "/userfeedback/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public UserFeedback getUserComment(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComment");

        return list.get(0);
    }

    @ApiOperation(
            value = "Provides an average rating for a metadata record",
            notes = "Provides an average rating for a metadata record",
            nickname = "getMetadataUserComments")
    @RequestMapping(
            value = "/metadata/{uuid}/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Rating getMetadataRating(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {
        
        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getMetadataUserComments");       

        return rating;
    }
    
    @ApiOperation(
            value = "Publishes a record, send notification ",
            notes = "Publishes a record, send notification ",
            nickname = "publish")
    @RequestMapping(
            value = "/userfeedback/{uuid}/publish",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseBody
    public boolean publish(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {
        
        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "publish");

        return true;
    }
    
    
    // PUT
    
    @ApiOperation(
            value = "Create a userfeedback (draft), send notification to owner ",
            notes = "Create a userfeedback (draft), send notification to owner ",
            nickname = "newUserFeedback")
    @RequestMapping(
            value = "/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean newUserFeedback(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        
        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "newUserFeedback");

        return true;
    }
    
    
    // DELETE
    @ApiOperation(
            value = "Removes a record",
            notes = "Removes a record",
            nickname = "deleteUserFeedback")
    @RequestMapping(
            value = "/userfeedback/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean deleteUserFeedback(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "deleteUserFeedback");

        return true;
    }
    
    // TODO: REMOVE Mockup data
    private static List<UserFeedback> list = new ArrayList<UserFeedback>();
    
    private static Rating rating = new Rating();
    
    static {
        UserFeedback uf1;
        list.add(uf1 = new UserFeedback());
        uf1.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
        User u1;
        uf1.setUser(u1 = new User());
        u1.setName("Marco Polo");
        u1.setOrganisation("SomethingGeo");
        
        UserFeedback uf2;
        list.add(uf2 = new UserFeedback());
        uf2.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
        User u2;
        uf2.setUser(u2 = new User());
        u2.setName("Cristoforo Colombo");
        u2.setOrganisation("GeoWhathever");       
        
        UserFeedback uf3;
        list.add(uf3 = new UserFeedback());
        uf3.setComment("Indeed");
        uf3.setUser(u1);
        
        UserFeedback uf4;
        list.add(uf4 = new UserFeedback());
        uf4.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
        uf4.setUser(u2);
        
        UserFeedback uf5;
        list.add(uf5 = new UserFeedback());
        uf5.setComment("Yes, you're right");
        uf5.setUser(u1);
        
        UserFeedback uf6;
        list.add(uf6 = new UserFeedback());
        uf6.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
        uf6.setUser(u2);
        
        
        
        
        rating.setAvgRating(4);
        
        rating.setCommentsCount(list.size());

    
    }
    
    
    // ************************
    
}
