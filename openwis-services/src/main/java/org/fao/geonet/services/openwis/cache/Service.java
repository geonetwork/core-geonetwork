/**
 * 
 */
package org.fao.geonet.services.openwis.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fao.geonet.services.openwis.cache.Request.ColumnCriterias;
import org.fao.geonet.services.openwis.cache.Request.OrderCriterias;
import org.fao.geonet.services.openwis.cache.Request.SearchCriterias;
import org.openwis.cacheindex.client.CacheIndexClient;
import org.openwis.cacheindex.client.CachedFile;
import org.openwis.cacheindex.client.CachedFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.services.ReadWriteController;

/**
 * Interact with the cache index SOAP service
 * 
 * @author delawen
 * 
 * 
 */
@Controller("openwis.cache")
@ReadWriteController
public class Service {

    @Autowired
    private CacheIndexClient client;

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
    @RequestMapping(value = { "/{lang}/openwis.cache.search" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Response search(@ModelAttribute Request request,
            HttpSession session) {

        // Check ajax concurrency
        Integer oldDraw = (Integer) session
                .getAttribute("openwis-cache-search-draw");
        // if (oldDraw != null && oldDraw > request.getDraw()) {
        // throw new ConcurrencyFailureException(
        // oldDraw + " > " + request.getDraw());
        // }
        session.setAttribute("openwis-cache-search-draw", request.getDraw());

        // Setup fields to search, filter and order.
        String sortOrder = "ASC";
        String sortField = "filename";

        if (!request.getOrder().isEmpty()) {
            sortOrder = request.getOrder().get(0).get(OrderCriterias.dir);
            Integer tmpIndex = Integer.valueOf(
                    request.getOrder().get(0).get(OrderCriterias.column));
            if (request.getColumns().size() >= tmpIndex) {
                String tmp = request.getColumns().get(tmpIndex)
                        .get(ColumnCriterias.name);
                if (tmp != null && !tmp.trim().isEmpty()) {
                    sortField = tmp;
                }
            }
        }

        System.out.println(request.getSearch().get(SearchCriterias.regex));
        System.out.println(request.getSearch().get(SearchCriterias.value));
        
        // TODO metadata filter
        String metadataFilter = "";

        // TODO metadata filter
        String fileNameFilter = "";
        
        
//        cf.filename, cf.checksum, cf.received_from_gts, cf.insertion_date
        
        
        Response response = new Response();
        response.setDraw(request.getDraw());
        response.setRecordsTotal(client.getTotal());
        response.setRecordsFiltered(
                client.getTotalCurrentQuery(metadataFilter, fileNameFilter));

        List<CachedFileInfo> list = client.retrieveCacheContentFilteredSorted(
                request.getStart(), request.getLength(), sortField, sortOrder,
                metadataFilter, fileNameFilter);
        for (CachedFileInfo cfi : list) {
            Map<String, String> element = new HashMap<String, String>();
            element.put("filename", cfi.getName());
            element.put("checksum", cfi.getChecksum());
            element.put("origin", cfi.getOrigin());
            element.put("urn", cfi.getMetadataUrn());
            XMLGregorianCalendar insertionDate = cfi.getInsertionDate();
            element.put("insertion_date", insertionDate.getDay() + "/"
                    + insertionDate.getMonth() + "/" + insertionDate.getYear());
            response.addData(element);
        }

        return response;
    }

    @RequestMapping(value = { "/{lang}/openwis.cache.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody CachedFile search(@RequestParam Long id) {

        return client.retrieveCachedFileById(id);

    }

    public CacheIndexClient getClient() {
        return client;
    }

    public void setClient(CacheIndexClient client) {
        this.client = client;
    }
}
