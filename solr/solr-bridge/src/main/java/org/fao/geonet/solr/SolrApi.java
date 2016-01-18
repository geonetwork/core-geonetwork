package org.fao.geonet.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by francois on 18/01/16.
 */
@Controller
public class SolrApi {

    @Autowired
    SolrJProxy solrProxy;

    @Autowired
    SolrConfig solrConfig;

    @RequestMapping(value = "/{uiLang}/solrproxy/ping")
    @ResponseBody
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
}
