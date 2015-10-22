package org.openwis.request.client;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Request web service.
 *
 * @author María Arias de Reyna
 */
public class RequestClient extends WebServiceGatewaySupport {
    /**
     * Retrieves the last processed request.
     *
     * @param subscriptionId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Long create(String urn, AdHoc adHoc) {
        ObjectFactory objFact = new ObjectFactory();

        CreateRequest request = objFact.createCreateRequest();
        request.setAdHoc(adHoc);
        request.setMetadataURN(urn);

        JAXBElement<CreateRequestResponse> response = (JAXBElement<CreateRequestResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(objFact.createCreateRequest(request));
        CreateRequestResponse responseType = response.getValue();

        return responseType.getReturn();
    }

}
