/**
 * 
 */
package org.fao.geonet.services.openwis.blacklisting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.fao.geonet.services.openwis.util.Request;
import org.fao.geonet.services.openwis.util.Request.OrderCriterias;
import org.fao.geonet.services.openwis.util.Request.SearchCriterias;
import org.fao.geonet.services.openwis.util.Response;
import org.openwis.blacklist.client.BlacklistClient;
import org.openwis.blacklist.client.BlacklistInfo;
import org.openwis.blacklist.client.BlacklistInfoResult;
import org.openwis.blacklist.client.BlacklistStatus;
import org.openwis.blacklist.client.SetUserBlacklistedResponse;
import org.openwis.blacklist.client.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    /**
     * Get all the dissemination data of a user
     * 
     * @param startWith
     * @param firstResult
     * @param maxResults
     * @param column
     * @param direction
     * @return
     */
    @RequestMapping(value = {
            "/{lang}/openwis.blacklisting.search" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Response search(@ModelAttribute Request request,
            HttpServletRequest httpRequest) {

        SortDirection sort = SortDirection.DESC;

        sort = SortDirection.valueOf(request.getOrder().get(0)
                .get(OrderCriterias.dir).toUpperCase());

        String startWith = request.getSearch().get(SearchCriterias.value);
        Integer firstResult = request.getStart();
        Integer maxResults = request.getLength();

        List<BlacklistInfo> list = null;
        Response response = new Response();
        response.setDraw(request.getDraw());

        BlacklistInfoResult resTotal = client.retrieveUsersBlackListInfoByUser(
                firstResult, maxResults, sort);
        response.setRecordsTotal((long) resTotal.getCount());

        if (startWith == null || startWith.trim().isEmpty()) {
            list = resTotal.getList();
            response.setRecordsFiltered((long) resTotal.getCount());
        } else {
            BlacklistInfoResult resFiltered = client
                    .retrieveUsersBlackListInfoByUser(firstResult, maxResults,
                            sort, startWith);
            list = resFiltered.getList();
            response.setRecordsFiltered((long) resFiltered.getCount());
        }

        for (BlacklistInfo bli : list) {
            Map<String, String> element = new HashMap<String, String>();
            element.put("user", bli.getUser());
            element.put("nbDisseminationWarnThreshold",
                    Long.toString(bli.getNbDisseminationWarnThreshold()));
            element.put("volDisseminationWarnThreshold",
                    Long.toString(bli.getVolDisseminationWarnThreshold()));
            element.put("nbDisseminationBlacklistThreshold",
                    Long.toString(bli.getNbDisseminationBlacklistThreshold()));
            element.put("volDisseminationBlacklistThreshold",
                    Long.toString(bli.getVolDisseminationBlacklistThreshold()));
            element.put("id", bli.getId().toString());
            if (bli.getStatus() != null) {
                element.put("status", bli.getStatus().value());
            } else {
                element.put("status", "");
            }
            response.addData(element);
        }

        return response;

    }

    /**
     * Check if a user is blacklisted
     * 
     * @param user
     * @return
     */
    @RequestMapping(value = {
            "/{lang}/openwis.blacklisting.isBlacklisted" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Boolean isBLacklisted(@RequestParam String user) {

        return client.getBlacklisted(user).isReturn();

    }

    /**
     * Update the dissemination data
     * 
     * @param user
     * @param id
     * @param nbBlacklist
     * @param nbWarn
     * @param volWarn
     * @param volBlacklist
     * @return
     */
    public BlacklistInfo set(String user, Long id, Long nbBlacklist,
            Long nbWarn, Long volWarn, Long volBlacklist,
            BlacklistStatus status) {

        BlacklistInfo info = new BlacklistInfo();
        info.setId(id);
        info.setUser(user);
        info.setNbDisseminationBlacklistThreshold(nbBlacklist);
        info.setNbDisseminationWarnThreshold(nbWarn);
        info.setVolDisseminationBlacklistThreshold(volBlacklist);
        info.setVolDisseminationWarnThreshold(volWarn);
        info.setStatus(status);

        return client.updateUserBlackListInfo(info);
    }

    /**
     * Update only if the user is blacklisted.
     * 
     * @param user
     * @param isBlacklisted
     * @return
     */
    public SetUserBlacklistedResponse set(String user, Boolean isBlacklisted) {

        return client.setBlacklisted(isBlacklisted, user);
    }

    /**
     * Everything together.
     * 
     * @param user
     * @param id
     * @param isBlacklisted
     * @param nbBlacklist
     * @param nbWarn
     * @param volWarn
     * @param volBlacklist
     * @param status
     * @return
     */
    @RequestMapping(value = { "/{lang}/openwis.blacklisting.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody BlacklistInfo set(@RequestParam String user,
            @RequestParam Long id, @RequestParam Boolean isBlacklisted,
            @RequestParam(value = "nbDisseminationBlacklistThreshold") Long nbBlacklist,
            @RequestParam(value = "nbDisseminationWarnThreshold") Long nbWarn,
            @RequestParam(value = "volDisseminationWarnThreshold") Long volWarn,
            @RequestParam(value = "volDisseminationBlacklistThreshold") Long volBlacklist,
            @RequestParam BlacklistStatus status) {

        set(user, isBlacklisted);
        return set(user, id, nbBlacklist, nbWarn, volWarn, volBlacklist,
                status);
    }

    public BlacklistClient getClient() {
        return client;
    }

    public void setClient(BlacklistClient client) {
        this.client = client;
    }
}
