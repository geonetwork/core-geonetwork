package org.fao.geonet.services.openwis.subscription;


import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.services.openwis.DataListResponse;
import org.fao.geonet.services.openwis.product.ProductMetadataDTO;
import org.fao.geonet.services.openwis.product.ProductMetadataNotFoundException;
import org.jdom.Element;
import org.openwis.metadata.product.ProductMetadataManager;
import org.openwis.products.client.ProductMetadata;
import org.openwis.subscription.client.SortDirection;
import org.openwis.subscription.client.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Controller class for Subscriptions.
 *
 * @author Jose García
 */
@Controller("openwis.subscription")
public class SubscriptionController {


    @Autowired
    private SubscriptionManager manager;

    @RequestMapping(value = { "/{lang}/openwis.subscription.search" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    DataListResponse list(@PathVariable String lang,
                          @RequestParam(value = "iDisplayStart") Integer start,
                          @RequestParam(value = "iDisplayLength", required = false) Integer maxRecords,
                          HttpServletRequest request) {

        // Data-tables start param starts at 0

        // TODO: Manage sorting

        DataListResponse<Subscription> response = new DataListResponse<>();

        List<Subscription> subscriptions =
                manager.retrieveSubscriptionsByUsers(start, maxRecords, SortDirection.ASC);
        response.addAllData(subscriptions);

        return response;
    }


    @RequestMapping(value = { "/{lang}/openwis.subscription.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    Subscription retrieve(@PathVariable String lang,
                             @RequestParam Long subscriptionId) {


        Subscription subscription = manager.retrieveSubscription(subscriptionId);
        if (subscription == null) throw new SubscriptionNotFoundException();

        return subscription;
    }

    @RequestMapping(value = { "/{lang}/openwis.subscription.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    OkResponse save(@PathVariable String lang,
                    @RequestParam Long subscriptionId) {


        Subscription subscription = manager.retrieveSubscription(subscriptionId);

        if (subscription != null) {
            // TODO: Set subscription fields

            manager.save(subscription);
        } else {
            throw new SubscriptionNotFoundException();
        }

        return new OkResponse();
    }


}
