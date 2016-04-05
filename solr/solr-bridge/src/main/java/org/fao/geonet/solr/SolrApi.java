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

package org.fao.geonet.solr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrClient;
import org.fao.geonet.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

/**
 * Created by francois on 18/01/16.
 */
@RequestMapping(value = {
        "/api/search",
        "/api/" + API.VERSION_0_1 + "/search"
})
@Api(value = "search",
        tags= "search",
        description = "Catalog search operations")
@Controller
public class SolrApi {

    @Autowired
    SolrJProxy solrProxy;

    @Autowired
    SolrConfig solrConfig;

    @RequestMapping(value = "/ping",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiOperation(value = "Ping search index",
                  nickname = "pingIndex")
    public boolean pingSolr() throws Exception {
        if (solrProxy != null) {
            try {
                solrProxy.ping();
                return true;
            } catch (Exception e) {
                throw new Exception(
                        String.format("Failed to ping Solr at URL %s. " +
                                        "Check Solr configuration.",
                                solrConfig.getSolrServerUrl()),
                        e);
            }
        } else {
            throw new Exception(String.format("No Solr client URL defined in %s. " +
                    "Check bean configuration.", solrConfig.getSolrServerUrl()));
        }
    }

    @RequestMapping(value = "/update",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete documents",
            nickname = "delete")
    public void delete(@RequestParam(required = true)
                       String query,
                       HttpServletRequest request) throws Exception {


        // TODO: Check if user can delete documents first
//        throw new SecurityException(String.format(
//                "Current user can't remove document with filter '%s'.", filter));

        SolrClient client = solrProxy.getServer();
        client.deleteByQuery(query);
        client.commit();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            Exception.class
    })
    public Object unauthorizedHandler(final Exception exception) {
        exception.printStackTrace();
        return new LinkedHashMap<String, String>() {{
            put("code", "index-is-down");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }
}
