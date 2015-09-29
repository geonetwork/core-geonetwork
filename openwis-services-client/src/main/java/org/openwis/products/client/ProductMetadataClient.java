package org.openwis.products.client;

import javax.xml.bind.JAXBElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Client for ProductMetadata web service.
 *
 * @author Jose García
 */
public class ProductMetadataClient {

    private WebServiceTemplate webServiceTemplate;

    /**
     * Retrieves a product metadata by the metadata urn.
     *
     * @param metadataUrn
     *            Metadata Urn
     * @return
     */
    public ProductMetadata retrieveProductMetadataByUrn(String metadataUrn) {
        ObjectFactory objFact = new ObjectFactory();

        GetProductMetadataByUrn request = objFact
                .createGetProductMetadataByUrn();
        request.setProductMetadataUrn(metadataUrn);

        @SuppressWarnings("unchecked")
        JAXBElement<GetProductMetadataByUrnResponse> response = (JAXBElement<GetProductMetadataByUrnResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetProductMetadataByUrn(request));
        GetProductMetadataByUrnResponse responseType = response.getValue();

        return responseType.getReturn();

    }

    /**
     * Creates a product metadata.
     *
     * @param productMetadata
     * @return
     */
    public long createProductMetadata(ProductMetadata productMetadata) {
        ObjectFactory objFact = new ObjectFactory();

        CreateProductMetadata request = objFact.createCreateProductMetadata();
        request.setProductMetadata(productMetadata);

        @SuppressWarnings("unchecked")
        JAXBElement<CreateProductMetadataResponse> response = (JAXBElement<CreateProductMetadataResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createCreateProductMetadata(request));
        CreateProductMetadataResponse responseType = response.getValue();

        return responseType.getReturn();

    }

    /**
     * Updates an existing product metadata.
     *
     * @param productMetadata
     */
    public UpdateProductMetadataResponse updateProductMetadata(
            ProductMetadata productMetadata) {
        ObjectFactory objFact = new ObjectFactory();

        UpdateProductMetadata request = objFact.createUpdateProductMetadata();
        request.setProductMetadata(productMetadata);

        @SuppressWarnings("unchecked")
        JAXBElement<UpdateProductMetadataResponse> response = (JAXBElement<UpdateProductMetadataResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createUpdateProductMetadata(request));
        return response.getValue();
    }

    /**
     * Deletes a product metadata using the product metadata identifier.
     *
     * @param productMetadataId
     */
    public DeleteProductMetadataResponse deleteProductMetadataById(
            Long productMetadataId) {
        ObjectFactory objFact = new ObjectFactory();

        DeleteProductMetadata request = objFact.createDeleteProductMetadata();
        request.setProductMetadataId(productMetadataId);

        @SuppressWarnings("unchecked")
        JAXBElement<DeleteProductMetadataResponse> response = (JAXBElement<DeleteProductMetadataResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createDeleteProductMetadata(request));
        return response.getValue();
    }

    /**
     * Deletes a product metadata using the metadata URN.
     *
     * @param metadataUrn
     *            Metadata Urn
     */
    public DeleteProductMetadataByURNResponse deleteProductMetadataByURN(
            String metadataUrn) {
        ObjectFactory objFact = new ObjectFactory();

        DeleteProductMetadataByURN request = objFact
                .createDeleteProductMetadataByURN();
        request.setProductMetadataUrn(metadataUrn);

        @SuppressWarnings("unchecked")
        JAXBElement<DeleteProductMetadataByURNResponse> response = (JAXBElement<DeleteProductMetadataByURNResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createDeleteProductMetadataByURN(request));

        return response.getValue();
    }

    public WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
}
