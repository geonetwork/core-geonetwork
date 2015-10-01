/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.ecore.xml.type.internal.XMLCalendar;
import org.openwis.monitoring.client.AlarmEventType;
import org.openwis.monitoring.client.DisseminatedDataType;
import org.openwis.monitoring.client.IngestedDataType;
import org.openwis.monitoring.client.MonitoringClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
public class Get {

    @Autowired
    private MonitoringClient client;

    final private static BigInteger maxRec = new BigInteger("100");

    @RequestMapping(value = { "/{lang}/openwis.monitoring.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody IResponse get(@RequestParam String monitorType,
            @RequestParam(required = false) String iniDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) BigInteger maxRecords) {

        IResponse response = null;

        if (maxRecords == null) {
            maxRecords = Get.maxRec;
        }

        switch (monitorType) {
        case "catalogStatistics":
            String catalogStatistics = client.retrieveCatalogStatistics(group);
            response = new CatalogStatistics();
            ((CatalogStatistics) response).setData(catalogStatistics);
            break;
        case "dataStatistics":
            response = new DataStatistics();
            List<DisseminatedDataType> dataStatistics = client
                    .retrieveDisseminatedDataStatistics(maxRecords);
            ((DataStatistics) response).setData(dataStatistics);
            break;
        case "cacheContents":
            response = new CacheContents();
            String cacheContents = client.retrieveGetCacheContents(group);
            ((CacheContents) response).setData(cacheContents);
            break;
        case "cacheStatistics":
            response = new CacheStatistics();
            String cacheStatistics = client.retrieveGetCacheStatistics(group);
            ((CacheStatistics) response).setData(cacheStatistics);
            break;
        case "globalReports":
            response = new GlobalReports();
            String globalReports = client.retrieveGlobalReports(group);
            ((GlobalReports) response).setData(globalReports);
            break;
        case "ingestedDataStatistics":
            response = new IngestedDataStatistics();
            List<IngestedDataType> ingestedDataStatistics = client
                    .retrieveIngestedDataStatistics(maxRecords);
            ((IngestedDataStatistics) response).setData(ingestedDataStatistics);
            break;
        case "recentEvents":
            response = new RecentEvents();
            XMLGregorianCalendar from = new XMLCalendar(iniDate, XMLCalendar.DATE);
            XMLGregorianCalendar to = new XMLCalendar(endDate, XMLCalendar.DATE);
            List<AlarmEventType> recentEvents = client
                    .retrieveRecentEvents(maxRecords, from, to);
            ((RecentEvents) response).setData(recentEvents);
            break;
        }

        return response;
    }
}
