package org.openwis.cacheindex.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Client for CacheIndex web service.
 *
 * @author Jose García
 */
public class CacheIndexClient extends WebServiceGatewaySupport {

    /**
     * Retrieves all cached files.
     *
     * @return
     */
    public List<CachedFile> listAllCachedFiles() {
        ObjectFactory objFact = new ObjectFactory();

        ListAllCachedFiles request = objFact.createListAllCachedFiles();

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createListAllCachedFiles(request));
        ListAllCachedFilesResponse responseType = (ListAllCachedFilesResponse) response
                .getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cached content count.
     *
     * @param metadataFilter
     * @param fileNameFilter
     * @return
     */
    public long retrieveCacheContentCount(String metadataFilter,
            String fileNameFilter) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request = objFact.createGetCacheContentCount();
        request.setMetadataFilterExpression(metadataFilter);
        request.setFilenameFilterExpression(fileNameFilter);

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentCount(request));
        GetCacheContentCountResponse responseType = (GetCacheContentCountResponse) response
                .getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a cached file by the id.
     *
     * @param id
     * @return
     */
    public CachedFile retrieveCachedFileById(Long id) {
        ObjectFactory objFact = new ObjectFactory();

        GetCachedFileById request = objFact.createGetCachedFileById();
        request.setArg0(id);

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCachedFileById(request));
        GetCachedFileByIdResponse responseType = (GetCachedFileByIdResponse) response
                .getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a list of cached file info paginated/sorted.
     *
     * @param firstResult
     * @param maxResults
     * @param sortField
     * @param sortOrder
     * @return
     */
    public List<CachedFileInfo> retrieveCacheContentSorted(int firstResult,
            int maxResults, String sortField, String sortOrder) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentSorted request = objFact.createGetCacheContentSorted();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortField(sortField);
        request.setSortOrder(sortOrder);

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentSorted(request));
        GetCacheContentSortedResponse responseType = (GetCacheContentSortedResponse) response
                .getValue();

        return responseType.getReturn();
    }

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
        if(metadataFilter != null) {
            request.setMetadataFilterExpression(metadataFilter);
        }
        
        if(fileNameFilter != null) {
            request.setFilenameFilterExpression(fileNameFilter);
        }
        
        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentFilteredSorted(request));
        GetCacheContentFilteredSortedResponse responseType = (GetCacheContentFilteredSortedResponse) response
                .getValue();

        return responseType.getReturn();
    }

    public long getTotal() {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request = objFact.createGetCacheContentCount();
        request.setFilenameFilterExpression(null);
        request.setMetadataFilterExpression(null);

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentCount(request));
        GetCacheContentCountResponse responseType = (GetCacheContentCountResponse) response
                .getValue();

        return responseType.getReturn();
    }

    public long getTotalCurrentQuery(String metadataFilterExpression,
            String fileNameFilterExpression) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request = objFact.createGetCacheContentCount();
        request.setFilenameFilterExpression(fileNameFilterExpression);
        request.setMetadataFilterExpression(metadataFilterExpression);

        JAXBElement response = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(
                        objFact.createGetCacheContentCount(request));
        GetCacheContentCountResponse responseType = (GetCacheContentCountResponse) response
                .getValue();

        return responseType.getReturn();
    }
}
