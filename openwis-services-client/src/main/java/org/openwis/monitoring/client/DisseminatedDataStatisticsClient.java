package org.openwis.monitoring.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.openwis.disseminatedDataStatistics.client.GetDisseminatedDataStatistics;
import org.openwis.disseminatedDataStatistics.client.GetDisseminatedDataStatisticsResponse;
import org.openwis.disseminatedDataStatistics.client.ObjectFactory;
import org.openwis.disseminatedDataStatistics.client.UserDisseminationData;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Monitoring web service.
 *
 * This class doesn't use the Object Factory as others as the generated classes
 * have @XmlRootElement annotation.
 *
 * @author María
 */
public class DisseminatedDataStatisticsClient extends WebServiceGatewaySupport {

    public List<UserDisseminationData> getDisseminatedDataStatistics(
            Integer maxRecords) {
        ObjectFactory objFact = new ObjectFactory();

        GetDisseminatedDataStatistics request = objFact
                .createGetDisseminatedDataStatistics();
        request.setMaxItemsCount(maxRecords);

        @SuppressWarnings("unchecked")
        JAXBElement<GetDisseminatedDataStatisticsResponse> response = (JAXBElement<GetDisseminatedDataStatisticsResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetDisseminatedDataStatistics(request));
        GetDisseminatedDataStatisticsResponse responseType = response.getValue();


        return responseType.getReturn();
    }

}
