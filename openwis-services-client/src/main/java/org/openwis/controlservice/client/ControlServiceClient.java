package org.openwis.controlservice.client;

import org.openwis.controlservice.client.*;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.JAXBElement;

/**
 * Client for ControlService web service.
 *
 * @author Jose García
 */
public class ControlServiceClient {

    private WebServiceTemplate webServiceTemplate;

    /**
     * Adds an ingestion filter.
     *
     * @param description
     * @param regex
     *
     * @return
     */
    public boolean addIngestionFilter(String description, String regex) {
        ObjectFactory objFact = new ObjectFactory();

        AddIngestionFilter request = objFact
                .createAddIngestionFilter();
        request.setDescription(description);
        request.setRegex(regex);

        @SuppressWarnings("unchecked")
        JAXBElement<AddIngestionFilterResponse> response = (JAXBElement<AddIngestionFilterResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createAddIngestionFilter(request));
        AddIngestionFilterResponse responseType = response.getValue();

        return responseType.isReturn();
    }

    /**
     * Adds a feeding filter.
     *
     * @param description
     * @param regex
     *
     * @return
     */
    public boolean addFeedingFilter(String description, String regex) {
        ObjectFactory objFact = new ObjectFactory();

        AddFeedingFilter request = objFact
                .createAddFeedingFilter();
        request.setDescription(description);
        request.setRegex(regex);

        @SuppressWarnings("unchecked")
        JAXBElement<AddFeedingFilterResponse> response = (JAXBElement<AddFeedingFilterResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createAddFeedingFilter(request));
        AddFeedingFilterResponse responseType = response.getValue();

        return responseType.isReturn();
    }



    public WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
}
