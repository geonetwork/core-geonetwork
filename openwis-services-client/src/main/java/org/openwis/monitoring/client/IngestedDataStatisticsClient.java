package org.openwis.monitoring.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.openwis.ingestedDataStatistics.client.GetIngestedDataStatistics;
import org.openwis.ingestedDataStatistics.client.GetIngestedDataStatisticsResponse;
import org.openwis.ingestedDataStatistics.client.IngestedData;
import org.openwis.ingestedDataStatistics.client.IngestedDataColumn_0020;
import org.openwis.ingestedDataStatistics.client.ObjectFactory;
import org.openwis.ingestedDataStatistics.client.SortDirection;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Monitoring web service.
 *
 * This class doesn't use the Object Factory as others as the generated classes
 * have @XmlRootElement annotation.
 *
 * @author María
 */
public class IngestedDataStatisticsClient extends WebServiceGatewaySupport {

    public List<IngestedData> getIngestedDataStatistics(Integer maxRecords,
            int firstResult, SortDirection sort, String column) {

        ObjectFactory objFact = new ObjectFactory();

        GetIngestedDataStatistics request = objFact
                .createGetIngestedDataStatistics();
        request.setMaxItemsCount(maxRecords);
        request.setDir(sort);
        request.setFirstResult(firstResult);
        column = column.toUpperCase();
        request.setColumn(IngestedDataColumn_0020.fromValue(column));

        @SuppressWarnings("unchecked")
        JAXBElement<GetIngestedDataStatisticsResponse> response = (JAXBElement<GetIngestedDataStatisticsResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetIngestedDataStatistics(request));
        GetIngestedDataStatisticsResponse responseType = response.getValue();

        return responseType.getReturn();
    }

}
