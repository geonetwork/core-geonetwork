/**
 * 
 */
package org.fao.geonet.services.openwis.blacklisting;

import java.util.List;

import org.openwis.blacklist.client.BlacklistClient;
import org.openwis.blacklist.client.BlacklistInfo;
import org.openwis.blacklist.client.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.services.ReadWriteController;

/**
 * Interact with the blacklisting SOAP service
 * 
 * @author delawen
 * 
 * 
 */
@Controller("openwis.blacklisting")
@ReadWriteController
public class Service {
    
    @Autowired
    private BlacklistClient client;
    
    @RequestMapping(value = { "/{lang}/openwis.blacklisting.search" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<BlacklistInfo> search(
            @RequestParam(required = false) String startWith,
            @RequestParam(required=false, defaultValue="0") Integer firstResult,
            @RequestParam(required=false, defaultValue="20") Integer maxResults,
            @RequestParam(required=false, defaultValue="") String column,
            @RequestParam(required=false, defaultValue="ASC") String direction) {
       
        SortDirection sort = SortDirection.fromValue(direction);
        return client.retrieveUsersBlackListInfoByUser(firstResult, maxResults, sort);

    }    

    @RequestMapping(value = { "/{lang}/openwis.blacklisting.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody BlacklistInfo set(
            @RequestParam BlacklistInfo info) {

        
        return info;

    }

    public BlacklistClient getClient() {
        return client;
    }

    public void setClient(BlacklistClient client) {
        this.client = client;
    }
}
