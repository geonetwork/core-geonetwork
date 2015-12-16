/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fao.geonet.services.openwis.util.Request;
import org.fao.geonet.services.openwis.util.Request.ColumnCriterias;
import org.fao.geonet.services.openwis.util.Request.OrderCriterias;
import org.fao.geonet.services.openwis.util.Response;
import org.openwis.disseminatedDataStatistics.client.UserDisseminationData;
import org.openwis.exchangedDataStatistics.client.ExchangedData;
import org.openwis.ingestedDataStatistics.client.IngestedData;
import org.openwis.ingestedDataStatistics.client.SortDirection;
import org.openwis.monitoring.client.DisseminatedDataStatisticsClient;
import org.openwis.monitoring.client.ExchangedDataStatisticsClient;
import org.openwis.monitoring.client.IngestedDataStatisticsClient;
import org.openwis.monitoring.client.ReplicatedDataStatisticsClient;
import org.openwis.replicatedDataStatistics.client.ReplicatedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.services.ReadWriteController;

/**
 * 
 * Extract monitoring/statistics information from SOAP service.
 * 
 */

@Controller("openwis.monitoring")
@ReadWriteController
public class Service {

    @Autowired
    private DisseminatedDataStatisticsClient dclient;
    @Autowired
    private ExchangedDataStatisticsClient eclient;
    @Autowired
    private IngestedDataStatisticsClient iclient;
    @Autowired
    private ReplicatedDataStatisticsClient rclient;

    private String[] keys = { "date", "size", "dissToolNbFiles", "userId",
            "dissToolSize", "id", "nbFiles", "source", "nbMetadata",
            "totalSize" };

    @RequestMapping(value = { "/{lang}/openwis.monitoring.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Response get(@RequestParam String monitorType,
            @ModelAttribute Request request, HttpServletRequest httpRequest) {

        Response res = new Response();
        res.setDraw(request.getDraw());

        // Setup fields to search, filter and order.
        String sortOrder = "ASC";
        String sortField = "none";

        if (!request.getOrder().isEmpty()) {
            sortOrder = request.getOrder().get(0).get(OrderCriterias.dir)
                    .toUpperCase();
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

        switch (monitorType) {
        case "ingestedDataStatistics":
            SortDirection sort = SortDirection.valueOf(sortOrder);
            List<IngestedData> ingestedDataStatistics = iclient
                    .getIngestedDataStatistics(request.getLength(),
                            request.getStart(), sort, sortField);
            // res.setRecordsTotal(client.getTotal());
            // res.setRecordsFiltered(client.getTotalCurrentQuery(startWith));
            for (IngestedData o : ingestedDataStatistics) {
                Map<String, String> element = new HashMap<String, String>();
                element.put("size", o.getSize().toString());
                XMLGregorianCalendar date = o.getDate();
                element.put("date", date.getDay() + "/" + date.getMonth() + "/"
                        + date.getYear());
                res.addData(element);
            }
            break;
        case "disseminatedDataStatistics":
            List<UserDisseminationData> disseminatedDataStatistics = dclient
                    .getDisseminatedDataStatistics(request.getLength());
            // res.setRecordsTotal(client.getTotal());
            // res.setRecordsFiltered(client.getTotalCurrentQuery(startWith));
            for (UserDisseminationData o : disseminatedDataStatistics) {
                Map<String, String> element = new HashMap<String, String>();
                element.put("size", o.getSize().toString());
                XMLGregorianCalendar date = o.getDate();
                element.put("date", date.getDay() + "/" + date.getMonth() + "/"
                        + date.getYear());
                element.put("dissToolNbFiles",
                        o.getDissToolNbFiles().toString());
                element.put("userId", o.getUserId());
                element.put("dissToolSize", o.getDissToolSize().toString());
                element.put("id", o.getId().toString());
                element.put("nbFiles", o.getNbFiles().toString());
                res.addData(element);
            }
            break;
        case "exchangedDataStatistics":
            List<ExchangedData> exchangedDataStatistics = eclient
                    .getExchangedDataStatistics(request.getLength());
            // res.setRecordsTotal(client.getTotal());
            // res.setRecordsFiltered(client.getTotalCurrentQuery(startWith));
            for (ExchangedData o : exchangedDataStatistics) {
                Map<String, String> element = new HashMap<String, String>();
                element.put("source", o.getSource());
                XMLGregorianCalendar date = o.getDate();
                element.put("date", date.getDay() + "/" + date.getMonth() + "/"
                        + date.getYear());
                element.put("id", o.getId().toString());
                element.put("nbMetadata", o.getNbMetadata().toString());
                element.put("totalSize", o.getTotalSize().toString());
                res.addData(element);
            }
            break;
        case "replicatedDataStatistics":
            List<ReplicatedData> replicatedDataStatistics = rclient
                    .getReplicatedDataStatistics(request.getLength(),
                            org.openwis.replicatedDataStatistics.client.SortDirection
                                    .valueOf(sortOrder),
                            request.getStart(), sortField);

            // res.setRecordsTotal(client.getTotal());
            // res.setRecordsFiltered(client.getTotalCurrentQuery(startWith));
            for (ReplicatedData o : replicatedDataStatistics) {
                Map<String, String> element = new HashMap<String, String>();
                element.put("source", o.getSource());
                XMLGregorianCalendar date = o.getDate();
                element.put("date", date.getDay() + "/" + date.getMonth() + "/"
                        + date.getYear());
                element.put("size", o.getSize().toString());
                res.addData(element);
            }
            break;
        }

        for (Map<String, String> map : res.getData()) {
            for (String key : keys) {
                if (!map.containsKey(key)) {
                    map.put(key, "");
                }
            }
        }

        return res;
    }
}
