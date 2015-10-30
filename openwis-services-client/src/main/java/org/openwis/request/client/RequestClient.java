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
     * Save a new request.
     *
     * @param urn
     * @param adhoc
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

    /**
     * Discard request.
     *
     * @param id
     * @return
     */
    public Boolean discard(Long id) {
        ObjectFactory objFact = new ObjectFactory();

        DeleteRequest request = objFact.createDeleteRequest();
        request.setRequestId(id);

        getWebServiceTemplate()
                .marshalSendAndReceive(objFact.createDeleteRequest(request));

        return true;
    }

    /**
     * Get request.
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public AdHoc get(Long id) {
        ObjectFactory objFact = new ObjectFactory();

        GetRequest request = objFact.createGetRequest();
        request.setRequestId(id);

        JAXBElement<GetRequestResponse> response = (JAXBElement<GetRequestResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(objFact.createGetRequest(request));
        GetRequestResponse responseType = response.getValue();

        return responseType.getReturn();
    }
}
