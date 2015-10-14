package org.openwis.monitoring.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.openwis.replicatedDataStatistics.client.GetReplicatedDataStatistics;
import org.openwis.replicatedDataStatistics.client.GetReplicatedDataStatisticsResponse;
import org.openwis.replicatedDataStatistics.client.ObjectFactory;
import org.openwis.replicatedDataStatistics.client.ReplicatedData;
import org.openwis.replicatedDataStatistics.client.ReplicatedDataColumn_0020;
import org.openwis.replicatedDataStatistics.client.SortDirection;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Monitoring web service.
 *
 * This class doesn't use the Object Factory as others as the generated classes
 * have @XmlRootElement annotation.
 *
 * @author María
 */
public class ReplicatedDataStatisticsClient extends WebServiceGatewaySupport {

    public List<ReplicatedData> getReplicatedDataStatistics(int maxRecords,
            SortDirection sort, int firstResult, String column) {
        ObjectFactory objFact = new ObjectFactory();

        GetReplicatedDataStatistics request = objFact
                .createGetReplicatedDataStatistics();
        request.setMaxItemsCount(maxRecords);
        request.setDir(sort);
        request.setFirstResult(firstResult);
        column = column.toUpperCase();
        request.setColumn(ReplicatedDataColumn_0020.valueOf(column));

        @SuppressWarnings("unchecked")
        JAXBElement<GetReplicatedDataStatisticsResponse> response = (JAXBElement<GetReplicatedDataStatisticsResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetReplicatedDataStatistics(request));
        GetReplicatedDataStatisticsResponse responseType = response.getValue();

        return responseType.getReturn();
    }
}