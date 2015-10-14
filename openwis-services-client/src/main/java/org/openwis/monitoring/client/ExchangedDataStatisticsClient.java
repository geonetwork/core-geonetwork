package org.openwis.monitoring.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.openwis.exchangedDataStatistics.client.ExchangedData;
import org.openwis.exchangedDataStatistics.client.GetExchangedDataStatistics;
import org.openwis.exchangedDataStatistics.client.GetExchangedDataStatisticsResponse;
import org.openwis.exchangedDataStatistics.client.ObjectFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Monitoring web service.
 *
 * This class doesn't use the Object Factory as others as the generated classes
 * have @XmlRootElement annotation.
 *
 * @author María
 */
public class ExchangedDataStatisticsClient extends WebServiceGatewaySupport {

    public List<ExchangedData> getExchangedDataStatistics(Integer maxRecords) {

        ObjectFactory objFact = new ObjectFactory();
        GetExchangedDataStatistics request = objFact
                .createGetExchangedDataStatistics();
        request.setMaxItemsCount(maxRecords);

        @SuppressWarnings("unchecked")
        JAXBElement<GetExchangedDataStatisticsResponse> response = (JAXBElement<GetExchangedDataStatisticsResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetExchangedDataStatistics(request));
        GetExchangedDataStatisticsResponse responseType = response.getValue();

        return responseType.getReturn();
    }

}
