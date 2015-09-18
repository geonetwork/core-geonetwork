package org.openwis.products.client;

import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;

/**
 * Client for ProductMetadata web service.
 *
 * @author Jose García
 */
public class ProductMetadataClient extends WebServiceGatewaySupport {

    /**
     * Retrieves a product metadata by the metadata urn.
     *
     * @param metadataUrn   Metadata Urn
     * @return
     */
    public ProductMetadata retrieveProductMetadataByUrn(String metadataUrn) {
        ObjectFactory objFact = new ObjectFactory();

        GetProductMetadataByUrn request =  objFact.createGetProductMetadataByUrn();
        request.setProductMetadataUrn(metadataUrn);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetProductMetadataByUrn(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetProductMetadataByUrnResponse responseType = (GetProductMetadataByUrnResponse) response.getValue();

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

        CreateProductMetadata request =  objFact.createCreateProductMetadata();
        request.setProductMetadata(productMetadata);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createCreateProductMetadata(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        CreateProductMetadataResponse responseType = (CreateProductMetadataResponse) response.getValue();

      return responseType.getReturn();

    }

    /**
     * Updates an existing product metadata.
     *
     * @param productMetadata
     */
    public void updateProductMetadata(ProductMetadata productMetadata) {
        ObjectFactory objFact = new ObjectFactory();

        UpdateProductMetadata request =  objFact.createUpdateProductMetadata();
        request.setProductMetadata(productMetadata);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createUpdateProductMetadata(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        UpdateProductMetadataResponse responseType = (UpdateProductMetadataResponse) response.getValue();
    }

    /**
     * Deletes a product metadata using the product metadata identifier.
     *
     * @param productMetadataId
     */
    public void deleteProductMetadataById(Long productMetadataId) {
        ObjectFactory objFact = new ObjectFactory();

        DeleteProductMetadata request =  objFact.createDeleteProductMetadata();
        request.setProductMetadataId(productMetadataId);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createDeleteProductMetadata(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        DeleteProductMetadataResponse responseType = (DeleteProductMetadataResponse) response.getValue();
    }

    /**
     * Deletes a product metadata using the metadata URN.
     *
     * @param metadataUrn   Metadata Urn
     */
    public void deleteProductMetadataByURN(String metadataUrn) {
        ObjectFactory objFact = new ObjectFactory();

        DeleteProductMetadataByURN request =  objFact.createDeleteProductMetadataByURN();
        request.setProductMetadataUrn(metadataUrn);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createDeleteProductMetadataByURN(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));


        DeleteProductMetadataByURNResponse responseType = (DeleteProductMetadataByURNResponse) response.getValue();
    }
}
