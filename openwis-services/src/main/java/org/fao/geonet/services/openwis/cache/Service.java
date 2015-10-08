/**
 * 
 */
package org.fao.geonet.services.openwis.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fao.geonet.services.openwis.cache.Request.ColumnCriterias;
import org.fao.geonet.services.openwis.cache.Request.OrderCriterias;
import org.openwis.cacheindex.client.CacheIndexClient;
import org.openwis.cacheindex.client.CachedFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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
            HttpServletRequest httpRequest) {

        // if using sAjax, this is needed
        request.populate(httpRequest);

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

        // FIXME this is not used
        String metadataFilter = "";

        StringBuilder fileNameFilter = new StringBuilder();

        for (Map<ColumnCriterias, String> column : request.getColumns()) {
            if (!column.get(ColumnCriterias.searchValue).isEmpty()) {
                if (column.get(ColumnCriterias.name) != "urn") {
                    if (fileNameFilter.length() != 0) {
                        fileNameFilter.append(" AND ");
                    }
                    fileNameFilter.append(column.get(ColumnCriterias.name)
                            + " like '%"
                            + column.get(ColumnCriterias.searchValue) + "%'");
                }
            }
        }

        if (request.getsSearch() != null
                && !request.getsSearch().trim().isEmpty()) {
            if (fileNameFilter.length() != 0) {
                fileNameFilter.append(" AND ");
            }
            fileNameFilter.append("(filename LIKE '%" + request.getsSearch()
                    + "%' OR checksum LIKE '%" + request.getsSearch() + "%')");
        }

        Response response = new Response();
        response.setDraw(request.getDraw());
        response.setRecordsTotal(client.getTotal());
        response.setRecordsFiltered(client.getTotalCurrentQuery(metadataFilter,
                fileNameFilter.toString()));

        List<CachedFileInfo> list = client.retrieveCacheContentFilteredSorted(
                request.getStart(), request.getLength(), sortField, sortOrder,
                metadataFilter, fileNameFilter.toString());
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
}
