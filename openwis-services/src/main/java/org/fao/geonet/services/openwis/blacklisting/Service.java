/**
 * 
 */
package org.fao.geonet.services.openwis.blacklisting;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.openwis.blacklist.client.BlacklistInfo;
import org.openwis.blacklist.client.BlacklistStatus;
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
    @RequestMapping(value = { "/{lang}/openwis.blacklisting.search" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<BlacklistInfo> search(
            @RequestParam(required = false) String startWith,
            @RequestParam(required=false, defaultValue="0") Integer firstResult,
            @RequestParam(required=false, defaultValue="20") Integer maxResults,
            @RequestParam(required=false, defaultValue="") String column,
            @RequestParam(required=false, defaultValue="ASC") String direction) {

        List<BlacklistInfo> result = new LinkedList<BlacklistInfo>();
        
        Random random = new Random();
        
        for(long i = 0; i < maxResults; i++) {
            BlacklistInfo bli = new BlacklistInfo();
            
            bli.setId(i);
            bli.setNbDisseminationBlacklistThreshold(random.nextInt(20) * 100);
            bli.setNbDisseminationWarnThreshold(random.nextInt(20) * 100);
            bli.setStatus(BlacklistStatus.NOT_BLACKLISTED);
            bli.setUser(startWith + "user " + i);
            bli.setVolDisseminationBlacklistThreshold(random.nextLong());
            bli.setVolDisseminationWarnThreshold(random.nextLong());
            
            result.add(bli);
        }
        
        return result;

    }    

    @RequestMapping(value = { "/{lang}/openwis.blacklisting.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody BlacklistInfo set(
            @RequestParam BlacklistInfo info) {

        
        return info;

    }
}
