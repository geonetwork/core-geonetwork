package org.openwis.processedrequest.client;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for Request web service.
 *
 * @author María Arias de Reyna
 */
public class ProcessedRequestClient extends WebServiceGatewaySupport {
    /**
     * Check if the request has already finished
     *
     * @param subscriptionId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean isFinished(Long id) {
        ObjectFactory objFact = new ObjectFactory();

        MonitorExtraction request = objFact.createMonitorExtraction();
        request.setProcessedRequestId(id);

        JAXBElement<MonitorExtractionResponse> response = (JAXBElement<MonitorExtractionResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createMonitorExtraction(request));
        MonitorExtractionResponse responseType = response.getValue();

        return responseType.isReturn();
    }

    /**
     * Get the URL of the processed request once it has finished
     *
     * @param subscriptionId
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getURL(Long id) {
        ObjectFactory objFact = new ObjectFactory();
        String uri = null;

        try {

            GetProcessedRequest request = objFact.createGetProcessedRequest();
            request.setId(id);

            JAXBElement<GetProcessedRequestResponse> response = (JAXBElement<GetProcessedRequestResponse>) getWebServiceTemplate()
                    .marshalSendAndReceive(
                            objFact.createGetProcessedRequest(request));
            GetProcessedRequestResponse responseType = response.getValue();

            ProcessedRequest res = responseType.getReturn();
            uri = res.getUri();
        } catch (Throwable t) {
            GetProcessedRequestForAdhoc request = objFact
                    .createGetProcessedRequestForAdhoc();
            request.setId(id);

            JAXBElement<GetProcessedRequestForAdhocResponse> response = (JAXBElement<GetProcessedRequestForAdhocResponse>) getWebServiceTemplate()
                    .marshalSendAndReceive(
                            objFact.createGetProcessedRequestForAdhoc(request));
            GetProcessedRequestForAdhocResponse responseType = response
                    .getValue();

            ProcessedRequest res = responseType.getReturn();
            uri = res.getUri();
        }
        return uri;
    }

}
