/**
 * 
 */
package org.fao.geonet.services.openwis.controlservice;

import jeeves.services.ReadWriteController;
import org.openwis.controlservice.client.ControlServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Interact with the blacklisting SOAP service
 * 
 * @author delawen
 * 
 * 
 */
@Controller("openwis.controlservice")
@ReadWriteController
public class ControlServiceController {

    @Autowired
    private ControlServiceClient client;

    /**
     *
     * @param lang
     * @param description
     * @param regex
     * @return
     */
    @RequestMapping(value = { "/{lang}/openwis.controlservice.saveingestionfilter" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    Boolean saveIngestFilter(@PathVariable String lang,
                                @RequestParam String description,
                                @RequestParam String regex) {

        return client.addIngestionFilter(description, regex);

    }

    /**
     *
     * @param lang
     * @param description
     * @param regex
     * @return
     */
    @RequestMapping(value = { "/{lang}/openwis.controlservice.savefeedingfilter" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    Boolean saveFeedingFilter(@PathVariable String lang,
                             @RequestParam String description,
                             @RequestParam String regex) {

        return client.addFeedingFilter(description, regex);

    }


    public ControlServiceClient getClient() {
        return client;
    }

    public void setClient(ControlServiceClient client) {
        this.client = client;
    }
}
