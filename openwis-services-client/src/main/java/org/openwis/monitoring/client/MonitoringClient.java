package org.openwis.monitoring.client;


import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

/**
 * Client for Monitoring web service.
 *
 * This class doesn't use the Object Factory as others as the generated classes have @XmlRootElement annotation.
 *
 * @author Jose García
 */
public class MonitoringClient extends WebServiceGatewaySupport {




    /**
     * Retrieves the recent events (alarms) gathered by the system.
     *
     * @param maxRecords
     * @param startTime
     * @param endTime
     * @return  The recent events (alarms) gathered by the system.
     */
    // TODO: Check about time parameters
    // TODO: Check if the report should be returned also?
    public List<AlarmEventType> retrieveRecentEvents(BigInteger maxRecords,
                                                XMLGregorianCalendar startTime,
                                                XMLGregorianCalendar endTime) {
        GetRecentEvents request = new GetRecentEvents();
        request.setMaxRecordCount(maxRecords);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        // No need ot objFact.createGetRecentEvents(request), the generated class has @XmlRootElement annotation
        GetRecentEventsResponse response = (GetRecentEventsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getElements();
    }


    /**
     * Retrieve the statistics (reports) gathered by the system.
     *
     * @param inValue
     * @return
     */
    public String retrieveGlobalReports(String inValue) {
        GetGlobalReports request =  new GetGlobalReports();
        request.setIn(inValue);

        GetGlobalReportsResponse response = (GetGlobalReportsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getOut();
    }

    /**
     * Retrieves the volume of data disseminated and extracted per day.
     *
     * @param maxRecords
     * @return
     */
    // TODO: Check if the report should be returned also?
    public List<ExchangedDataType> getExchangedDataStatistics(BigInteger maxRecords) {

        GetExchangedDataStatistics request =  new GetExchangedDataStatistics();
        request.setMaxRecordCount(maxRecords);

        GetExchangedDataStatisticsResponse response = (GetExchangedDataStatisticsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getElements();
    }

    /**
     * Retrieves the volume of data disseminated per day and per user.
     *
     * @param maxRecords
     * @return
     */
    // TODO: Check if the report should be returned also?
    public List<DisseminatedDataType> retrieveDisseminatedDataStatistics(BigInteger maxRecords) {

        GetDisseminatedDataStatistics request =  new GetDisseminatedDataStatistics();
        request.setMaxRecordCount(maxRecords);

        GetDisseminatedDataStatisticsResponse response = (GetDisseminatedDataStatisticsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getElements();
    }

    /**
     * Retrieves the volume of data ingested per day.
     *
     * @param maxRecords
     * @return
     */
    // TODO: Check if the report should be returned also?
    public List<IngestedDataType> retrieveIngestedDataStatistics(BigInteger maxRecords) {

        GetIngestedDataStatistics request =  new GetIngestedDataStatistics();
        request.setMaxRecordCount(maxRecords);

        GetIngestedDataStatisticsResponse response = (GetIngestedDataStatisticsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getElements();
    }


    /**
     *
     * @param inValue
     * @return
     */
    public String retrieveCatalogStatistics(String inValue) {

        GetCatalogStatistics request =  new GetCatalogStatistics();
        request.setIn(inValue);

        GetCatalogStatisticsResponse response = (GetCatalogStatisticsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getOut();
    }

    /**
     *
     * @param inValue
     * @return
     */
    public String retrieveGetCacheStatistics(String inValue) {

        GetCacheStatistics request =  new GetCacheStatistics();
        request.setIn(inValue);

        GetCacheStatisticsResponse response = (GetCacheStatisticsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getOut();
    }

    /**
     *
     * @param inValue
     * @return
     */
    public String retrieveGetCacheContents(String inValue) {

        GetCacheContents request =  new GetCacheContents();
        request.setIn(inValue);

        GetCacheContentsResponse response = (GetCacheContentsResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));

        return response.getOut();
    }


}
