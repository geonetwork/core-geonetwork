package org.openwis.cacheindex.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for CacheIndex web service.
 *
 * @author Jose García
 */
@SuppressWarnings("unchecked")
public class CacheIndexClient extends WebServiceGatewaySupport {

    /**
     * Retrieves the cache content filtered and sorted.
     *
     * @return
     */
    public List<CachedFileInfo> retrieveCacheContentFilteredSorted(
            int firstResult, int maxResults, String sortField, String sortOrder,
            String metadataFilter, String fileNameFilter) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentFilteredSorted request = objFact
                .createGetCacheContentFilteredSorted();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortField(sortField);
        request.setSortOrder(sortOrder);
        if (metadataFilter != null) {
            request.setMetadataFilterExpression(metadataFilter);
        }

        if (fileNameFilter != null) {
            request.setFilenameFilterExpression(fileNameFilter);
        }

        JAXBElement<GetCacheContentFilteredSortedResponse> response = (JAXBElement<GetCacheContentFilteredSortedResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentFilteredSorted(request));
        GetCacheContentFilteredSortedResponse responseType = response
                .getValue();

        return responseType.getReturn();
    }

    public long getTotal() {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request = objFact.createGetCacheContentCount();
        request.setFilenameFilterExpression(null);
        request.setMetadataFilterExpression(null);

        JAXBElement<GetCacheContentCountResponse> response = (JAXBElement<GetCacheContentCountResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentCount(request));
        GetCacheContentCountResponse responseType = response.getValue();

        return responseType.getReturn();
    }

    public long getTotalCurrentQuery(String metadataFilterExpression,
            String fileNameFilterExpression) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request = objFact.createGetCacheContentCount();
        request.setFilenameFilterExpression(fileNameFilterExpression);
        request.setMetadataFilterExpression(metadataFilterExpression);

        JAXBElement<GetCacheContentCountResponse> response = (JAXBElement<GetCacheContentCountResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentCount(request));
        GetCacheContentCountResponse responseType = response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cache content filtered and sorted.
     *
     * @return
     */
    public List<CachedFile> listFilesByMetadataUrnAndDate(String urn,
            String startDateString, String endDateString) {
        ObjectFactory objFact = new ObjectFactory();

        ListFilesByMetadataUrnAndDate request = objFact
                .createListFilesByMetadataUrnAndDate();
        request.setArg0(urn);
        request.setArg1(startDateString);
        request.setArg2(endDateString);
        
        JAXBElement<ListFilesByMetadataUrnAndDateResponse> response = (JAXBElement<ListFilesByMetadataUrnAndDateResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createListFilesByMetadataUrnAndDate(request));
        ListFilesByMetadataUrnAndDateResponse responseType = response
                .getValue();

        return responseType.getReturn();
    }
}
