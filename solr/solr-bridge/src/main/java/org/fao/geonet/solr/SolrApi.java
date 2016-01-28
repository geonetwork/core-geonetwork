package org.fao.geonet.solr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fao.geonet.services.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
