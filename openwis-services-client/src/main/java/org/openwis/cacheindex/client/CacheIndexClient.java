package org.openwis.cacheindex.client;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import java.util.List;

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

        ListAllCachedFiles request =  objFact.createListAllCachedFiles();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createListAllCachedFiles(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        ListAllCachedFilesResponse responseType = (ListAllCachedFilesResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cached files by metadata urn and date.
     *
     * @return
     */
    public List<CachedFile> listFilesByMetadataUrnAndDate() {
        ObjectFactory objFact = new ObjectFactory();

        ListFilesByMetadataUrnAndDate request =  objFact.createListFilesByMetadataUrnAndDate();
        // TODO: set arg0, arg1, check what values mean ...
        request.setArg0("");
        request.setArg1("");
        request.setArg2("");

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createListFilesByMetadataUrnAndDate(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        ListFilesByMetadataUrnAndDateResponse responseType = (ListFilesByMetadataUrnAndDateResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cached files by metadata urn and time.
     *
     * @return
     */
    public List<CachedFile> listFilesByMetadataUrnAndTime() {
        ObjectFactory objFact = new ObjectFactory();

        ListFilesByMetadataUrnAndTime request =  objFact.createListFilesByMetadataUrnAndTime();
        // TODO: set arg0, arg1, check what values mean ...
        request.setArg0("");
        request.setArg1("");


        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createListFilesByMetadataUrnAndTime(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        ListFilesByMetadataUrnAndTimeResponse responseType = (ListFilesByMetadataUrnAndTimeResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cached content count.
     *
     * @param metadataFilter
     * @param fileNameFilter
     * @return
     */
    public long retrieveCacheContentCount(String metadataFilter, String fileNameFilter) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentCount request =  objFact.createGetCacheContentCount();
        request.setMetadataFilterExpression(metadataFilter);
        request.setFilenameFilterExpression(fileNameFilter);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCacheContentCount(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCacheContentCountResponse responseType = (GetCacheContentCountResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a cached file.
     *
     * @return
     */
    public CachedFile retrieveCachedFile() {
        ObjectFactory objFact = new ObjectFactory();

        // TODO: Clarify the arguments
        GetCachedFile request =  objFact.createGetCachedFile();
        request.setArg0("");
        request.setArg1("");

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCachedFile(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCachedFileResponse responseType = (GetCachedFileResponse) response.getValue();

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

        GetCachedFileById request =  objFact.createGetCachedFileById();
        request.setArg0(id);


        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCachedFileById(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCachedFileByIdResponse responseType = (GetCachedFileByIdResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a list of cached files between dates.
     *
     * @return
     */
    public List<CachedFile> listCachedFilesBetweenDates() {
        ObjectFactory objFact = new ObjectFactory();

        ListCachedFilesBetweenDates request =  objFact.createListCachedFilesBetweenDates();
        // TODO: set arg0, arg1, check what values mean ...
        request.setArg0(1);
        request.setArg1(1);
        request.setArg2(null);
        request.setArg3(null);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createListCachedFilesBetweenDates(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        ListCachedFilesBetweenDatesResponse responseType = (ListCachedFilesBetweenDatesResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a list of cached files.
     *
     * @return
     */
    public List<CachedFile> listCachedFiles() {
        ObjectFactory objFact = new ObjectFactory();

        ListCachedFiles request =  objFact.createListCachedFiles();
        // TODO: set arg0, arg1, check what values mean ...
        request.setArg0(1);
        request.setArg1(1);
        request.setArg2(null);


        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createListCachedFiles(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        ListCachedFilesResponse responseType = (ListCachedFilesResponse) response.getValue();

        return responseType.getReturn();
    }


    /**
     *  Retrieves a list of cached file info paginated/sorted.
     *
     * @param firstResult
     * @param maxResults
     * @param sortField
     * @param sortOrder
     * @return
     */
    public List<CachedFileInfo> retrieveCacheContentSorted(int firstResult, int maxResults,
                                                           String sortField, String sortOrder) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentSorted request =  objFact.createGetCacheContentSorted();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortField(sortField);
        request.setSortOrder(sortOrder);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCacheContentSorted(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCacheContentSortedResponse responseType = (GetCacheContentSortedResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the cache content.
     *
     * @return
     */
    public List<CachedFileInfo> retrieveCacheContent() {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContent request =  objFact.createGetCacheContent();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCacheContent(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCacheContentResponse responseType = (GetCacheContentResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves the backup last collect date.
     *
     * @return
     */
    // TODO: Check to return java.util.Date?
    public long retrieveBackupLastCollectDate() {
        ObjectFactory objFact = new ObjectFactory();

        GetBackupLastCollectDate request =  objFact.createGetBackupLastCollectDate();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetBackupLastCollectDate(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetBackupLastCollectDateResponse responseType = (GetBackupLastCollectDateResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     *  Retrieves the cache content filtered and sorted.
     *
     * @return
     */
    public List<CachedFileInfo> retrieveCacheContentFilteredSorted(int firstResult, int maxResults,
                                                   String sortField, String sortOrder,
                                                   String metadataFilter, String fileNameFilter) {
        ObjectFactory objFact = new ObjectFactory();

        GetCacheContentFilteredSorted request =  objFact.createGetCacheContentFilteredSorted();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortField(sortField);
        request.setSortOrder(sortOrder);
        request.setMetadataFilterExpression(metadataFilter);
        request.setFilenameFilterExpression(fileNameFilter);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetCacheContentFilteredSorted(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetCacheContentFilteredSortedResponse responseType = (GetCacheContentFilteredSortedResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     *  Adds a cache index entry.
     *
     * @return
     */
    public CachedFile addCacheIndexEntry(FileInfo fileInfo) {
        ObjectFactory objFact = new ObjectFactory();

        AddCacheIndexEntry request =  objFact.createAddCacheIndexEntry();
        request.setArg0(fileInfo);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createAddCacheIndexEntry(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        AddCacheIndexEntryResponse responseType = (AddCacheIndexEntryResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     *  Sets the last collect date.
     *
     * @return
     */
    // TODO: Check to use java.util.Date as parameter?
    public void setLastCollectDate(long date) {
        ObjectFactory objFact = new ObjectFactory();

        SetLastCollectDate request =  objFact.createSetLastCollectDate();
        request.setArg0(date);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createSetLastCollectDate(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        SetLastCollectDateResponse responseType = (SetLastCollectDateResponse) response.getValue();
    }

    /**
     *  Backups the last collect date.
     *
     * @return
     */
    public void backupLastCollectDate() {
        ObjectFactory objFact = new ObjectFactory();

        BackupLastCollectDate request =  objFact.createBackupLastCollectDate();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createBackupLastCollectDate(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        BackupLastCollectDateResponse responseType = (BackupLastCollectDateResponse) response.getValue();
    }


    /**
     *
     * @return
     */
    public void ping() {
        ObjectFactory objFact = new ObjectFactory();

        Ping request =  objFact.createPing();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createPing(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        PingResponse responseType = (PingResponse) response.getValue();
    }
}
