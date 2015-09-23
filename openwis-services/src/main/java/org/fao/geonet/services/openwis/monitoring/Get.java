/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.xml.type.internal.XMLCalendar;
import org.openwis.monitoring.client.AlarmEventType;
import org.openwis.monitoring.client.DisseminatedDataType;
import org.openwis.monitoring.client.IngestedDataType;
import org.openwis.monitoring.client.MonitoringConfiguration;
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
    private MonitoringConfiguration config;

    final private static BigInteger maxRecords = new BigInteger("10000");

    @RequestMapping(value = { "/{lang}/openwis.monitoring.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody IResponse get(@RequestParam String monitorType,
            @RequestParam(required = false) String iniDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) BigInteger maxRecords) {

        IResponse response = null;

        if (maxRecords == null) {
            maxRecords = Get.maxRecords;
        }

        // MonitoringClient client = config.productMetadataClient();

        switch (monitorType) {
        case "catalogStatistics":
            // String catalogStatistics =
            // client.retrieveCatalogStatistics(group);
            response = new CatalogStatistics();
            ((CatalogStatistics)response).setData("No idea what goes here");
            // TODO
            break;
        case "dataStatistics":
            response = new DataStatistics();
            // List<DisseminatedDataType> dataStatistics = client
            // .retrieveDisseminatedDataStatistics(maxRecords);
            List<DisseminatedDataType> data = new LinkedList<DisseminatedDataType>();
            for(int i = 0; i < 30; i++) {
                DisseminatedDataType ddt = new DisseminatedDataType();
                ddt.setSize(i + 500l);
                ddt.setThreshold(1024l);
                ddt.setUser("user" + i);
                ddt.setDate(new XMLCalendar(new Date(), XMLCalendar.DATE));
                data.add(ddt);
            }
            ((DataStatistics)response).setData(data);
            // TODO
            break;
        case "cacheContents":
            response = new CacheContents();
            // String cacheContents = client.retrieveGetCacheContents(group);
            // TODO
            ((CacheContents)response).setData("No idea what cache contents data goes here");
            break;
        case "cacheStatistics":
            response = new CacheStatistics();
            // String cacheStatistics =
            // client.retrieveGetCacheStatistics(group);
            ((CacheStatistics)response).setData("No idea what cache statistics data goes here");
            // TODO
            break;
        case "globalReports":
            response = new GlobalReports();
            // String globalReports = client.retrieveGlobalReports(group); 
            ((GlobalReports)response).setData("No idea what global reports data goes here");
            // TODO
            break;
        case "ingestedDataStatistics":
            response = new IngestedDataStatistics();
            // List<IngestedDataType> ingestedDataStatistics = client
            // .retrieveIngestedDataStatistics(maxRecords);
            List<IngestedDataType> list = new LinkedList<IngestedDataType>();
            for(int i = 0; i < 30; i++) {
                IngestedDataType ddt = new IngestedDataType();
                ddt.setSize(i + 500l);
                ddt.setDate(new XMLCalendar(new Date(), XMLCalendar.DATE));
                list.add(ddt);
            }
            ((IngestedDataStatistics)response).setData(list);
            // TODO
            break;
        case "recentEvents":
            response = new RecentEvents();
            // List<AlarmEventType> recentEvents = client
            // .retrieveRecentEvents(maxRecords, null, null);
            List<AlarmEventType> alarms = new LinkedList<AlarmEventType>();
            for(int i = 0; i < 30; i++) {
                AlarmEventType ddt = new AlarmEventType();
                ddt.setComponent("Component" + i);
                ddt.setDescription("Description description description");
                ddt.setProcess("Process.....");
                ddt.setSeverity("Severity");
                ddt.setDate(new XMLCalendar(new Date(), XMLCalendar.DATE));
                alarms.add(ddt);
            }
            ((RecentEvents)response).setData(alarms);
            // TODO
            break;
        }

        return response;
    }
}
